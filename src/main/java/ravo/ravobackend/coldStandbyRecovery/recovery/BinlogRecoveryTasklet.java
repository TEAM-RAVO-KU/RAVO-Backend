package ravo.ravobackend.coldStandbyRecovery.recovery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ravo.ravobackend.global.domain.DatabaseProperties;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ravo.ravobackend.global.constants.JobExecutionContextKeys.BACKUP_OUT_FILE;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinlogRecoveryTasklet implements Tasklet {

    @Value("${backup.output-dir}")
    private String backupDir;

    private final DatabaseProperties activeDatabaseProperties;

    private final RecoveryStrategyFactory factory;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ExecutionContext jobContext = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        String dumpFile = jobContext.getString(BACKUP_OUT_FILE);
        log.info("Recovering from dumpFile={}", dumpFile);
        Path dumpPath = Paths.get(dumpFile);

        if (!Files.exists(dumpPath)) {
            throw new FileNotFoundException("SQL dump file not found: " + dumpPath);
        }
        log.info("Starting recovery from dump file: {}", dumpPath);

        // 3) 전략 결정 및 RecoveryTarget 생성
        RecoveryStrategy strategy = factory.getRecoveryStrategy(activeDatabaseProperties.getDriverClassName());

        // 4) 복구 실행
        strategy.recover(activeDatabaseProperties, dumpPath);
        log.info("Recovery completed successfully for database: {}", activeDatabaseProperties.getDatabase());

        return RepeatStatus.FINISHED;
    }
}
