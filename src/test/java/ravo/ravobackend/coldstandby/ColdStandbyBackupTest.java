package ravo.ravobackend.coldstandby;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.FileSystemUtils;
import ravo.ravobackend.RavoBackendApplication;
import ravo.ravobackend.coldstandby.config.ColdStandbyJobConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@SpringBootTest(
        classes = {ColdStandbyJobConfig.class, RavoBackendApplication.class},
        properties = {
                "spring.batch.job.enabled=false",
                "spring.datasource.url=jdbc:h2:tcp://localhost/~/batch",          // 테스트 전용
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.batch.job.enabled=false",
                "backup.output-dir=${java.io.tmpdir}/my-test-backup"
        }
)
@SpringBatchTest
public class ColdStandbyBackupTest {

    @AfterAll
    static void cleanUp() throws IOException {
        Path dir = Paths.get(System.getProperty("java.io.tmpdir"), "my-test-backup");
        if (Files.exists(dir)) {
            FileSystemUtils.deleteRecursively(dir);
        }
    }

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job coldStandbyBackupJob;
    @Autowired
    private Job coldStandbyRecoveryJob;

    @Test
    @DisplayName("ColdStandby Backup 전체 수행")
    void dumpBackupStepTest() {
        this.jobLauncherTestUtils.setJob(coldStandbyBackupJob);

        JobExecution jobExecution = jobLauncherTestUtils.launchStep("dumpBackupStep");

        Assertions.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }

    @Test
    @DisplayName("ColdStandby Recovery tasklet 전체 수행")
    void dumpRecoveryStepTest() {
        this.jobLauncherTestUtils.setJob(coldStandbyRecoveryJob);

        JobExecution jobExecution = jobLauncherTestUtils.launchStep("dumpRecoveryStep");

        Assertions.assertEquals("COMPLETED", jobExecution.getExitStatus().getExitCode());
    }
}
