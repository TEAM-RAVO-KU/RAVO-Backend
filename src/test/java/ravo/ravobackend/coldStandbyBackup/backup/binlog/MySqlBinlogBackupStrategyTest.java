package ravo.ravobackend.coldStandbyBackup.backup.binlog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ravo.ravobackend.coldStandbyBackup.backup.BackupRequest;
import ravo.ravobackend.global.domain.DatabaseProperties;
import ravo.ravobackend.global.util.CommandRequest;
import ravo.ravobackend.global.util.CommandResult;
import ravo.ravobackend.global.util.ShellCommandExecutor;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MySqlBinlogBackupStrategyTest {

    private ShellCommandExecutor shellCommandExecutor;
    private MySqlBinlogBackupStrategy strategy;
    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        shellCommandExecutor = Mockito.mock(ShellCommandExecutor.class);
        strategy = new MySqlBinlogBackupStrategy(shellCommandExecutor);
        tempDir = Files.createTempDirectory("backup-test");
    }

    @Test
    @DisplayName("mysqlbinlog 전략은 shell 명령어 실행기를 호출하여 백업 파일을 생성해야 한다")
    void should_execute_mysqlbinlog_and_create_backup_file() throws Exception {
        // given
        DatabaseProperties props = new DatabaseProperties();
        props.setHost("localhost");
        props.setPort("3306");
        props.setUsername("root");
        props.setPassword("pass");
        props.setDatabase("testdb");

        BackupRequest request = BackupRequest.builder()
                .props(props)
                .backupDir(tempDir)
                .gtidRange("uuid:101-120")
                .build();

        when(shellCommandExecutor.execute(any(CommandRequest.class)))
                .thenReturn(CommandResult.builder().exitCode(0).build());

        // when
        strategy.backup(request);

        // then
        verify(shellCommandExecutor, times(1))
                .execute(any(CommandRequest.class));
    }
}