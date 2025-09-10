package ravo.ravobackend.global.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import ravo.ravobackend.global.constants.BackupTarget;
import ravo.ravobackend.global.constants.JobParameterKeys;
import ravo.ravobackend.global.domain.DatabaseProperties;


import static ravo.ravobackend.global.constants.JobExecutionContextKeys.TARGET_DATABASE_PROPERTIES;

@Slf4j
@Component
@RequiredArgsConstructor
public class TargetDatabaseSelectorTasklet implements Tasklet {

    private final DatabaseProperties standbyDatabaseProperties;
    private final DatabaseProperties activeDatabaseProperties;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        ExecutionContext jobContext = contribution.getStepExecution().getJobExecution().getExecutionContext();
        String targetDatabase = contribution.getStepExecution().getJobParameters().getString(JobParameterKeys.TARGET);
        if(targetDatabase == null) targetDatabase = BackupTarget.STANDBY;
        DatabaseProperties props = targetDatabase.equals(BackupTarget.ACTIVE) ? activeDatabaseProperties : standbyDatabaseProperties;

        jobContext.put(TARGET_DATABASE_PROPERTIES, props);
        log.info("[TargetDatabaseSelectorTasklet] target database : {}:{}/{}", props.getHost(), props.getPort(), props.getDatabase());

        return RepeatStatus.FINISHED;
    }
}
