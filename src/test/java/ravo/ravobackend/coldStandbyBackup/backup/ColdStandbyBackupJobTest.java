package ravo.ravobackend.coldStandbyBackup.backup;


import org.junit.jupiter.api.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;


@SpringBootTest
@SpringBatchTest
public class ColdStandbyBackupJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @MockitoBean
    private BackupStrategyFactory factory;

    @MockitoBean
    private BackupStrategy mockStrategy;

    @Autowired
    private Job coldStandbyBackupJob;

    @BeforeEach
    void setupMock() throws Exception {
        given(factory.getBackupStrategy(any())).willReturn(mockStrategy);

        willDoNothing().given(mockStrategy).backup(any(),any());
    }

    @Test
    @DisplayName("ColdStandbyBackupJob이 성공적으로 실행된다")
    void coldStandbyBackupJob_shouldRunSuccessfully() throws Exception {
        this.jobLauncherTestUtils.setJob(coldStandbyBackupJob);

        JobExecution execution = jobLauncherTestUtils.launchJob();

        assertThat(execution.getExitStatus().getExitCode())
                .isEqualTo("COMPLETED");
    }
}