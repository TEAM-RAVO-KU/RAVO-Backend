package ravo.ravobackend.coldstandby.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ravo.ravobackend.coldstandby.backup.DumpBackupTasklet;
import ravo.ravobackend.coldstandby.recovery.DumpRecoveryTasklet;

@Configuration
public class ColdStandbyJobConfig {

    @Bean
    public Job coldStandbyBackupJob(JobRepository jobRepository, Step dumpBackupStep) {
        return new JobBuilder("coldStandbyBackupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(dumpBackupStep)
                .end()
                .build();
    }

    @Bean
    public Job coldStandbyRecoveryJob(JobRepository jobRepository, Step dumpRecoveryStep) {
        return new JobBuilder("coldStandbyRecoveryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(dumpRecoveryStep)
                .end()
                .build();
    }

    @Bean
    public Step dumpBackupStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, DumpBackupTasklet t) {
        return new StepBuilder("dumpBackupStep", jobRepository)
                .tasklet(t, platformTransactionManager)
                .build();
    }

    @Bean
    public Step dumpRecoveryStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, DumpRecoveryTasklet t) {
        return new StepBuilder("dumpRecoveryStep", jobRepository)
                .tasklet(t, platformTransactionManager)
                .build();
    }
}
