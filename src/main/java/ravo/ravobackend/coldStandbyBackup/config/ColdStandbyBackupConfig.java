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
import ravo.ravobackend.coldStandbyBackup.tasklet.DirectoryInitializerTasklet;
import ravo.ravobackend.coldStandbyBackup.tasklet.DumpBackupTasklet;
import ravo.ravobackend.global.tasklet.TargetDatabaseSelectorTasklet;

@Configuration
@RequiredArgsConstructor
public class ColdStandbyBackupConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job coldStandbyBackupJob(Step targetDatabaseSelectorStep, Step directoryInitializerStep, Step dumpBackupStep) {
        return new JobBuilder("coldStandbyBackupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(targetDatabaseSelectorStep)
                .next(directoryInitializerStep)
                .next(dumpBackupStep)
                .end()
                .build();
    }

    @Bean
    public Step dumpBackupStep(DumpBackupTasklet t) {
        return new StepBuilder("dumpBackupStep", jobRepository)
                .tasklet(t, transactionManager)
                .build();
    }

    @Bean
    public Step directoryInitializerStep(DirectoryInitializerTasklet t) {
        return new StepBuilder("directoryInitializerStep", jobRepository)
                .tasklet(t, transactionManager)
                .build();
    }

    @Bean
    public Step targetDatabaseSelectorStep(TargetDatabaseSelectorTasklet t) {
        return new StepBuilder("targetDatabaseSelectorStep", jobRepository)
                .tasklet(t, transactionManager)
                .build();
    }
}
