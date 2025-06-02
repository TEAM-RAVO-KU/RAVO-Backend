package ravo.ravobackend.coldStandbyBackup.backup;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldStandbyBackup.domain.BackupTarget;

import java.nio.file.Path;
import java.nio.file.Paths;

@RequiredArgsConstructor
@Component
public class DumpBackupTasklet implements Tasklet {

    @Value("${backup.output-dir}")
    private String outputDir;

    @Autowired
    @Qualifier("activeDataSourceProperties")
    private DataSourceProperties dataSourceProperties;

    private final BackupStrategyFactory factory;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        BackupStrategy backupStrategy = factory.getBackupStrategy(dataSourceProperties.getDriverClassName());
        BackupTarget backupTarget = backupStrategy.buildBackupTarget(dataSourceProperties);
        Path dumpPath = Paths.get(outputDir);
        backupStrategy.backup(backupTarget, dumpPath);

        return RepeatStatus.FINISHED;
    }
}
