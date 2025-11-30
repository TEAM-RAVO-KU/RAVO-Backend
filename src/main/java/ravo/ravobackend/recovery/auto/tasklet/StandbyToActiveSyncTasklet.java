package ravo.ravobackend.recovery.auto.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ravo.ravobackend.recovery.auto.domain.GTID;
import ravo.ravobackend.recovery.auto.service.BinlogQueryService;
import ravo.ravobackend.recovery.auto.service.GtidService;
import ravo.ravobackend.global.constants.TargetDB;
import ravo.ravobackend.global.constants.DatabaseProperties;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class StandbyToActiveSyncTasklet implements Tasklet {

    private static final long MAX_DUMP_BYTES = 50L * 1024 * 1024; // 50MB 제한 (필요 시 조정)

    private final GtidService gtidService;
    private final BinlogQueryService binlogQueryService;
    private final DatabaseProperties activeProps;
    private final DatabaseProperties standbyProps;

    public StandbyToActiveSyncTasklet(
            GtidService gtidService,
            BinlogQueryService binlogQueryService,
            @Qualifier("activeDatabaseProperties") DatabaseProperties activeProps,
            @Qualifier("standbyDatabaseProperties") DatabaseProperties standbyProps) {
        this.gtidService = gtidService;
        this.binlogQueryService = binlogQueryService;
        this.activeProps = activeProps;
        this.standbyProps = standbyProps;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        GTID currentGtid = gtidService.getCurrentGtidFromStandby();
        Optional<GTID> lastSavedOpt = gtidService.getLastSavedGtid(TargetDB.STANDBY);
        Optional<GTID> gtidRangeOpt = gtidService.calculateGtidRange(currentGtid, lastSavedOpt.orElse(null));

        // 1. standby DB의 (마지막 저장 ~ 현재) GTID 범위 계산
        if (gtidRangeOpt.isEmpty()) {
            // 차이 없으면 백업 스킵
            log.info("No new GTIDs found. Skipping standby -> active sync.");
            return RepeatStatus.FINISHED;
        }
        GTID gtidRange = gtidRangeOpt.get();

        // 2. standby DB에서 사용중인 모든 binlog 파일 이름 조회
        List<String> binlogFileNames = binlogQueryService.getBinlogFileNames();
        if (binlogFileNames.isEmpty()) {
            log.warn("[SYNC] No binlog files found on standby. Skipping sync.");
            return RepeatStatus.FINISHED;
        }

        // 3. mysqlbinlog 결과를 파일로 덤프
        File dumpFile = dumpBinlogToFile(gtidRange.toString(), standbyProps, binlogFileNames);

        try {
            // 4. 파일을 active DB mysql 에 먹이기
            applyDumpFileToActive(dumpFile, activeProps);

            // 5. 동기화 완료 후 현재 GTID 저장
            gtidService.saveGtid(TargetDB.STANDBY, currentGtid);
        } finally {
            if (dumpFile != null && dumpFile.exists()) {
                if (!dumpFile.delete()) {
                    log.warn("[SYNC] Failed to delete temp dump file: {}", dumpFile.getAbsolutePath());
                }
            }
        }

        return RepeatStatus.FINISHED;
    }

    private File dumpBinlogToFile(String gtidRangeStr,
                                  DatabaseProperties sourceProps,
                                  List<String> binlogFileNames) throws Exception {

        // OS 임시 디렉토리에 덤프 파일 생성
        File dumpFile = File.createTempFile("ravo-binlog-", ".sql");
        log.info("[SYNC] Dumping binlog to file: {}", dumpFile.getAbsolutePath());

        List<String> cmd = new ArrayList<>();
        cmd.add("mysqlbinlog");
        cmd.add("--read-from-remote-server");
        cmd.add("--host=" + sourceProps.getHost());
        cmd.add("--port=" + sourceProps.getPort());
        cmd.add("--user=" + sourceProps.getUsername());
        if (sourceProps.getPassword() != null && !sourceProps.getPassword().isBlank()) {
            cmd.add("--password=" + sourceProps.getPassword());
        }

        // GTID 범위 제한 + GTID 이벤트는 제거(Active에서 GTID 꼬임 방지)
        cmd.add("--skip-gtids");
        cmd.add("--include-gtids=" + gtidRangeStr);

        cmd.addAll(binlogFileNames);

        log.info("[SYNC] Running mysqlbinlog command (to file): {}", String.join(" ", cmd));

        ProcessBuilder pb = new ProcessBuilder(cmd);
        // stdout → 파일
        pb.redirectOutput(dumpFile);
        // stderr → 로그용으로 읽기
        pb.redirectErrorStream(false);

        Process proc = pb.start();

        StringBuilder stderrSb = new StringBuilder();
        Thread stderrThread = new Thread(() ->
                copyToLog(proc.getErrorStream(), stderrSb, "[SYNC][BINLOG][STDERR]"));
        stderrThread.start();

        boolean finished = proc.waitFor(2, TimeUnit.MINUTES);
        if (!finished) {
            proc.destroyForcibly();
            stderrThread.join();
            log.error("[SYNC] mysqlbinlog timed out. stderr:\n{}", stderrSb);
            throw new RuntimeException("mysqlbinlog timed out");
        }

        int exitCode = proc.exitValue();
        stderrThread.join();

        if (exitCode != 0) {
            log.error("[SYNC] mysqlbinlog failed. exitCode={}, stderr:\n{}", exitCode, stderrSb);
            throw new RuntimeException("mysqlbinlog failed. exitCode=" + exitCode);
        }

        long fileSize = dumpFile.length();
        if (fileSize > MAX_DUMP_BYTES) {
            log.error("[SYNC] Dump file too large. size={} bytes (> {} bytes).", fileSize, MAX_DUMP_BYTES);
            throw new RuntimeException("Dump file too large: " + fileSize + " bytes");
        }

        log.info("[SYNC] mysqlbinlog dump completed. fileSize={} bytes, file={}", fileSize, dumpFile.getAbsolutePath());
        return dumpFile;
    }

    private void applyDumpFileToActive(File dumpFile, DatabaseProperties activeProps) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("mysql");
        cmd.add("-h");
        cmd.add(activeProps.getHost());
        cmd.add("-P");
        cmd.add(String.valueOf(activeProps.getPort()));
        cmd.add("-u");
        cmd.add(activeProps.getUsername());
        if (activeProps.getPassword() != null && !activeProps.getPassword().isBlank()) {
            cmd.add("-p" + activeProps.getPassword());
        }
        cmd.add(activeProps.getDatabase());

        log.info("[SYNC] Applying dump file to active DB with command: {}", String.join(" ", cmd));
        log.info("[SYNC] Dump file path: {}", dumpFile.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        // stdin ← dumpFile
        pb.redirectInput(dumpFile);

        Process proc = pb.start();

        StringBuilder outSb = new StringBuilder();
        Thread outThread = new Thread(() ->
                copyToLog(proc.getInputStream(), outSb, "[SYNC][MYSQL]"));
        outThread.start();

        boolean finished = proc.waitFor(5, TimeUnit.MINUTES);
        if (!finished) {
            proc.destroyForcibly();
            outThread.join();
            log.error("[SYNC] mysql apply timed out. output:\n{}", outSb);
            throw new RuntimeException("mysql apply timed out");
        }

        int exitCode = proc.exitValue();
        outThread.join();

        log.info("[SYNC] mysql apply finished. exitCode={}", exitCode);
        log.info("[SYNC] mysql OUTPUT:\n{}", outSb);

        if (exitCode != 0) {
            throw new RuntimeException("mysql apply failed. exitCode=" + exitCode +
                    ", output=" + outSb);
        }
    }

    private void copyToLog(InputStream is, StringBuilder sb, String prefix) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
                log.info("{} {}", prefix, line);
            }
        } catch (IOException e) {
            log.warn("{} Failed to read process stream: {}", prefix, e.getMessage(), e);
        }
    }
}
