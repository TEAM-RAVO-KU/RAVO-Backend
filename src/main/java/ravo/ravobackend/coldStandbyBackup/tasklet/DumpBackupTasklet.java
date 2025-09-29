package ravo.ravobackend.coldStandbyBackup.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldStandbyBackup.backup.BackupStrategy;
import ravo.ravobackend.coldStandbyBackup.backup.BackupStrategyFactory;
import ravo.ravobackend.global.constants.BackupType;
import ravo.ravobackend.global.domain.DatabaseProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

import static ravo.ravobackend.global.constants.JobExecutionContextKeys.BACKUP_OUT_FILE;
import static ravo.ravobackend.global.constants.JobExecutionContextKeys.TARGET_DATABASE_PROPERTIES;

@RequiredArgsConstructor
@Component
public class DumpBackupTasklet implements Tasklet {

    private final BackupStrategyFactory factory;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ExecutionContext jobContext = contribution.getStepExecution().getJobExecution().getExecutionContext();
        DatabaseProperties props = (DatabaseProperties) jobContext.get(TARGET_DATABASE_PROPERTIES);
        Path backupDir = Paths.get(jobContext.getString(BACKUP_OUT_FILE));

        BackupStrategy backupStrategy = factory.getBackupStrategy(BackupType.MYSQL_DUMP);
        backupStrategy.backup(props, backupDir);

        return RepeatStatus.FINISHED;
    }
}
