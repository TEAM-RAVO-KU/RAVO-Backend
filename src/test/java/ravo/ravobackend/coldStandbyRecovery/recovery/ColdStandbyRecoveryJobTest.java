package ravo.ravobackend.coldStandbyRecovery.recovery;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

@SpringBootTest
@SpringBatchTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ColdStandbyRecoveryJobTest {

    @Autowired
    private Job coldStandbyRecoveryJob;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @MockitoBean
    private RecoveryStrategyFactory factory;

    @MockitoBean
    private RecoveryStrategy mockStrategy;

    static Path tempDir;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) throws IOException {
        tempDir = Files.createTempDirectory("backup-test-");
        registry.add("backup.output-dir", () -> tempDir.toString());
    }

    @BeforeEach
    void setupMock() throws Exception {
        given(factory.getRecoveryStrategy(any())).willReturn(mockStrategy);
        willDoNothing().given(mockStrategy).recover(any(),any());
    }

    @Test
    @DisplayName("ColdStandbyRecoveryJob이 성공적으로 실행된다")
    void coldStandbyRecoveryJob_shouldRunSuccessfully() throws Exception {
        Path dumpFilePath = Files.createTempFile(tempDir, "test_recovery", ".sql");
        Files.writeString(dumpFilePath, "-- dummy sql content");

        this.jobLauncherTestUtils.setJob(coldStandbyRecoveryJob);
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("dumpFile", dumpFilePath.getFileName().toString())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        Assertions.assertThat(jobExecution.getExitStatus().getExitCode())
                .isEqualTo("COMPLETED");
    }

    @AfterAll
    void cleanUp() throws IOException {
        // 테스트 종료 후 임시 디렉토리 및 파일 삭제
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); }
                        catch (IOException ignored) {}
                    });
        }
    }

}
