package ravo.ravobackend.coldStandbyRecovery.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ravo.ravobackend.coldStandbyRecovery.recovery.DumpRecoveryTasklet;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ColdStandbyRecoveryConfig {

    private final JobRepository jobRepository;

    private final PlatformTransactionManager transactionManager;

    private final DumpRecoveryTasklet recoveryTasklet;

    @Bean
    public Job coldStandbyRecoveryJob() {
        Step dumpRecoveryStep = new StepBuilder("dumpRecoveryStep", jobRepository)
                .tasklet(recoveryTasklet, transactionManager)
                .build();

        return new JobBuilder("coldStandbyRecoveryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(dumpRecoveryStep)
                .build();
    }
}
