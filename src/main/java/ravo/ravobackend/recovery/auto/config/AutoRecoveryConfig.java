package ravo.ravobackend.recovery.auto.config;

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
import ravo.ravobackend.recovery.auto.tasklet.StandbyToActiveSyncTasklet;

@Configuration
@RequiredArgsConstructor
public class AutoRecoveryConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job binlogRecoveryJob(Step standbyToActiveSyncStep) {
        return new JobBuilder("binlogRecoveryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(standbyToActiveSyncStep)
                .build();
    }

    @Bean
    public Step standbyToActiveSyncStep(StandbyToActiveSyncTasklet t) {
        return new StepBuilder("standbyToActiveSyncStep", jobRepository)
                .tasklet(t, transactionManager)
                .build();
    }
}
