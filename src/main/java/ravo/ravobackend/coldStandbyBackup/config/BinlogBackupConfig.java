package ravo.ravobackend.coldStandbyBackup.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ravo.ravobackend.coldStandbyBackup.tasklet.BinlogBackupTasklet;

@Configuration
@RequiredArgsConstructor
public class BinlogBackupConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job binlogBackupJob(Step targetDatabaseSelectorStep, Step directoryInitializerStep, Step binlogBackupStep) {
        return new JobBuilder("binlogBackupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(targetDatabaseSelectorStep)
                .next(directoryInitializerStep)
                .next(binlogBackupStep)
                .end()
                .build();
    }

    @Bean
    public Step binlogBackupStep(BinlogBackupTasklet t) {
        return new StepBuilder("binlogBackupStep", jobRepository)
                .tasklet(t, transactionManager)
                .build();
    }
}
