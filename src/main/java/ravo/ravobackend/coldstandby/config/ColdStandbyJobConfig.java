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
import ravo.ravobackend.coldstandby.tasklet.DumpBackupTasklet;

@Configuration
public class ColdStandbyJobConfig {

    @Bean
    public Job coldStandbyJob(JobRepository jobRepository, Step dumpStep) {
        return new JobBuilder("coldStandbyJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(dumpStep)
                .end()
                .build();
    }

    @Bean
    public Step dumpStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, DumpBackupTasklet t) {
        return new StepBuilder("dumpStep", jobRepository)
                .tasklet(t, platformTransactionManager)
                .build();
    }

}
