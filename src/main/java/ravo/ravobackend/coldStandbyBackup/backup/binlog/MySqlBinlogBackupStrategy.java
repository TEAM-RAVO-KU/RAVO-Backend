package ravo.ravobackend.coldStandbyBackup.backup.binlog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldStandbyBackup.backup.BackupRequest;
import ravo.ravobackend.coldStandbyBackup.backup.BackupStrategy;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.BinlogInfo;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.service.BinlogQueryService;
import ravo.ravobackend.global.constants.BackupType;
import ravo.ravobackend.global.domain.DatabaseProperties;
import ravo.ravobackend.global.util.CommandRequest;
import ravo.ravobackend.global.util.CommandResult;
import ravo.ravobackend.global.util.ShellCommandExecutor;

import java.nio.file.Path;
import java.util.*;

/**
 * MySQL Binary Log를 사용한 백업 전략
 * Point-in-time recovery를 위한 증분 백업에 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MySqlBinlogBackupStrategy implements BackupStrategy {

    private final ShellCommandExecutor shellCommandExecutor;
    private final BinlogQueryService binlogQueryService;

    @Override
    public boolean support(BackupType type) {
        return type.equals(BackupType.MYSQL_BINLOG);
    }

    @Override
    public void backup(BackupRequest request) throws Exception {
        DatabaseProperties props = request.getProps();
        Path backupDir = request.getBackupDir();
        String gtidRange = request.getGtidRange();

        List<BinlogInfo> binlogFiles = binlogQueryService.getBinlogFiles();

        boolean success = false;
        Exception lastError = null;

        for (BinlogInfo file : binlogFiles) {
            String fileName = file.getLogName();
            log.info("Trying GTID backup with binlog file: {}", fileName);

            try {
                backupBinlogByGtid(props, backupDir, gtidRange, fileName);
                success = true;
                log.info("GTID-based backup succeeded with file: {}", fileName);
                break; // 첫 성공 시 중단
            } catch (Exception e) {
                String msg = e.getMessage();
                log.warn("Binlog {} backup failed: {}", fileName, msg);

                // non-GTID binlog일 경우 계속 시도
                if (msg != null && msg.contains("Cannot replicate anonymous transaction")) {
                    log.info("Non-GTID binlog detected. Trying next file...");
                    continue;
                }

                // 다른 이유로 실패한 경우 기억해두고 종료
                lastError = e;
                break;
            }
        }

        if (!success) {
            String errMsg = (lastError != null)
                    ? lastError.getMessage()
                    : "No GTID-based binlog file succeeded.";
            throw new RuntimeException("All binlog backup attempts failed. " + errMsg);
        }
    }

    /**
     * GTID 기반 binlog 추출 (증분 백업)
     */
    private void backupBinlogByGtid(DatabaseProperties props,
                                    Path backupDir,
                                    String gtidRange,
                                    String binlogFile) throws Exception {

        List<String> cmd = buildMysqlBinlogGtidCommand(props, gtidRange, binlogFile);
        log.info("Executing mysqlbinlog for GTID range backup using file: {}", binlogFile);

        Map<String, String> env = new HashMap<>();
        env.put("MYSQL_PWD", props.getPassword()); // 비밀번호를 환경변수로 전달

        CommandResult result = shellCommandExecutor.execute(CommandRequest.builder()
                .command(cmd)
                .environmentVariables(env)
                .outputFile(backupDir.toFile())
                .build());

        if (result.getExitCode() != 0) {
            throw new RuntimeException(result.getErrorOutput());
        }

        log.info("Binlog GTID backup completed successfully ({}).", binlogFile);
    }

    private List<String> buildMysqlBinlogGtidCommand(DatabaseProperties props,
                                                     String gtidRange, String fileName) {
        List<String> cmd = new ArrayList<>();
        cmd.add("mysqlbinlog");
        cmd.add("--read-from-remote-source=BINLOG-DUMP-GTIDS");
        cmd.add("-h" + props.getHost());
        cmd.add("-P" + props.getPort());
        cmd.add("-u" + props.getUsername());
        cmd.add("--include-gtids=" + gtidRange);
        cmd.add(fileName);
        cmd.add("--to-last-log");
        return cmd;
    }
}
