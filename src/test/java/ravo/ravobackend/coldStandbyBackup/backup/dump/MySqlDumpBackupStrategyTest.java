package ravo.ravobackend.coldStandbyBackup.backup.dump;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ravo.ravobackend.coldStandbyBackup.backup.BackupRequest;
import ravo.ravobackend.global.domain.DatabaseProperties;
import ravo.ravobackend.global.util.CommandRequest;
import ravo.ravobackend.global.util.CommandResult;
import ravo.ravobackend.global.util.ShellCommandExecutor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MySqlDumpBackupStrategyTest {

    private ShellCommandExecutor shellCommandExecutor;
    private Path tempDir;
    private MySqlDumpBackupStrategy mySqlDumpBackupStrategy;

    @BeforeEach
    public void setup() throws IOException {
        shellCommandExecutor = Mockito.mock(ShellCommandExecutor.class);
        tempDir = Files.createTempDirectory("backup-test");
        mySqlDumpBackupStrategy = new MySqlDumpBackupStrategy(shellCommandExecutor);
    }

    @Test
    @DisplayName("mysqldump 전략은 shell 명령어 실행기를 호출하여 백업 파일을 생성해야 한다")
    public void should_execute_mysqldump_and_create_backup_file() throws Exception {
        //given
        DatabaseProperties props = new DatabaseProperties();
        props.setHost("localhost");
        props.setPort("3306");
        props.setUsername("root");
        props.setPassword("pass");
        props.setDatabase("testdb");

        BackupRequest req = BackupRequest.builder()
                .props(props)
                .backupDir(tempDir)
                .build();

        when(shellCommandExecutor.execute(any(CommandRequest.class)))
                .thenReturn(CommandResult.builder().exitCode(0).build());

        //when
        mySqlDumpBackupStrategy.backup(req);

        //then
        verify(shellCommandExecutor, times(1))
                .execute(any(CommandRequest.class));
    }

}