package ravo.ravobackend.coldStandbyBackup.backup;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ravo.ravobackend.global.domain.DatabaseProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Component
public class DumpBackupTasklet implements Tasklet {

    @Value("${backup.output-dir}")
    private String outputDir;

    private final DatabaseProperties standbyDatabaseProperties;

    private final BackupStrategyFactory factory;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        BackupStrategy backupStrategy = factory.getBackupStrategy(standbyDatabaseProperties.getDriverClassName());
        Path dumpPath = Paths.get(outputDir);
        backupStrategy.backup(standbyDatabaseProperties, dumpPath);

        return RepeatStatus.FINISHED;
    }
}
