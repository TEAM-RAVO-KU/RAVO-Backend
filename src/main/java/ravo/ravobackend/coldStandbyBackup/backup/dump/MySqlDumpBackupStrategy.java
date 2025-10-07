package ravo.ravobackend.coldStandbyBackup.backup.dump;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldStandbyBackup.backup.BackupRequest;
import ravo.ravobackend.coldStandbyBackup.backup.BackupStrategy;
import ravo.ravobackend.global.constants.BackupType;
import ravo.ravobackend.global.domain.DatabaseProperties;
import ravo.ravobackend.global.util.CommandRequest;
import ravo.ravobackend.global.util.ShellCommandExecutor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MySqlDumpBackupStrategy implements BackupStrategy {

    private final ShellCommandExecutor shellCommandExecutor;

    @Override
    public boolean support(BackupType type) {
        return type.equals(BackupType.MYSQL_DUMP);
    }

    @Override
    public void backup(BackupRequest request) throws Exception {
        DatabaseProperties props = request.getProps();
        Path backupDir = request.getBackupDir();

        List<String> cmd = buildMysqlDumpCommand(props);
        Map<String, String> env = new HashMap<>();
        env.put("MYSQL_PWD", props.getPassword()); // 비밀번호를 환경변수로 전달

        log.info("Starting MySQL backup to: {}", backupDir);

        shellCommandExecutor.execute(CommandRequest.builder()
                .command(cmd)
                .environmentVariables(env)
                .outputFile(backupDir.toFile())
                .build());
        log.info("MySQL backup completed successfully");
    }

    private List<String> buildMysqlDumpCommand(DatabaseProperties props) {
        List<String> cmd = new ArrayList<>();
        cmd.add("mysqldump");
        cmd.add("-h"); cmd.add(props.getHost());                     // 호스트 지정
        cmd.add("-P"); cmd.add(props.getPort());                     // 포트 지정
        cmd.add("-u"); cmd.add(props.getUsername());                 // 사용자 지정
        cmd.add("--add-drop-database");                              // 복원 시 DROP DATABASE 포함
        cmd.add("--databases"); cmd.add(props.getDatabase());        // 덤프할 데이터베이스 이름
        cmd.add("--single-transaction");                             // 인덱스 잠금 최소화
        cmd.add("--quick");                                          // 대용량 테이블도 빠르게 스트리밍
        return cmd;
    }
}
