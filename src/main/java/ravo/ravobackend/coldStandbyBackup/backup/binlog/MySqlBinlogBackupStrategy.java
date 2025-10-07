package ravo.ravobackend.coldStandbyBackup.backup.binlog;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldStandbyBackup.backup.BackupRequest;
import ravo.ravobackend.coldStandbyBackup.backup.BackupStrategy;
import ravo.ravobackend.global.constants.BackupType;
import ravo.ravobackend.global.domain.DatabaseProperties;
import ravo.ravobackend.global.util.CommandRequest;
import ravo.ravobackend.global.util.CommandResult;
import ravo.ravobackend.global.util.ShellCommandExecutor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MySQL Binary Log를 사용한 백업 전략
 * Point-in-time recovery를 위한 증분 백업에 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MySqlBinlogBackupStrategy implements BackupStrategy {

    private final ShellCommandExecutor shellCommandExecutor;

    @Override
    public boolean support(BackupType type) {
        return type.equals(BackupType.MYSQL_BINLOG);
    }

    @Override
    public void backup(BackupRequest request) throws Exception {
        DatabaseProperties props = request.getProps();
        Path backupDir = request.getBackupDir();
        String gtidRange = request.getGtidRange();
        String firstBinlogFile = request.getFirstBinlogFile();
        backupBinlogByGtid(props, backupDir, gtidRange, firstBinlogFile);
    }

    /**
     * GTID 기반 binlog 추출 (증분 백업)
     */
    public void backupBinlogByGtid(DatabaseProperties props,
                                   Path backupDir,
                                   String gtidRange,
                                   String firstBinlogFile) throws Exception {
        List<String> cmd = buildMysqlBinlogGtidCommand(props, gtidRange, firstBinlogFile);

        log.info("Executing mysqlbinlog for GTID range backup. Output: {}", backupDir);


        Map<String, String> env = new HashMap<>();
        env.put("MYSQL_PWD", props.getPassword()); // 비밀번호를 환경변수로 전달

        CommandResult result = shellCommandExecutor.execute(CommandRequest.builder()
                .command(cmd)
                .environmentVariables(env)
                .outputFile(backupDir.toFile())
                .build());

        if (result.getExitCode() != 0) {
            throw new RuntimeException("mysqlbinlog failed: " + result.getErrorOutput());
        }
        log.info("Binlog GTID backup completed. File: {}", backupDir);
    }


    private List<String> buildMysqlBinlogGtidCommand(DatabaseProperties props,
                                                     String gtidRange, String firstBinlogFile) {
        List<String> cmd = new ArrayList<>();
        cmd.add("mysqlbinlog");
        cmd.add("--read-from-remote-source=BINLOG-DUMP-GTIDS");
        cmd.add("-h" + props.getHost());
        cmd.add("-P" + props.getPort());
        cmd.add("-u" + props.getUsername());
        cmd.add("--include-gtids=" + gtidRange);
        cmd.add(firstBinlogFile);
        cmd.add("--to-last-log");
        return cmd;
    }
}
