package ravo.ravobackend.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ravo.ravobackend.global.FullBackupTasklet;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class FullBackupBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Step fullBackupStep(FullBackupTasklet fullBackupTasklet) {
        return new StepBuilder("fullBackupStep", jobRepository)
                .tasklet(fullBackupTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job fullBackupJob(Step fullBackupStep) {
        return new JobBuilder("fullBackupJob", jobRepository)
                .start(fullBackupStep)
                .build();
    }

}
