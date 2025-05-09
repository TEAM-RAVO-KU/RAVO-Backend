package ravo.ravobackend.coldStandbyRecovery.config;

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
import ravo.ravobackend.coldStandbyRecovery.recovery.DumpRecoveryTasklet;

@Configuration
@RequiredArgsConstructor
public class ColdStandbyRecoveryConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job coldStandbyRecoveryJob(Step dumpRecoveryStep) {
        return new JobBuilder("coldStandbyRecoveryJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(dumpRecoveryStep)
                .end()
                .build();
    }

    @Bean
    public Step dumpRecoveryStep(DumpRecoveryTasklet recoveryTasklet) {
        return new StepBuilder("dumpRecoveryStep", jobRepository)
                .tasklet(recoveryTasklet, transactionManager)
                .build();
    }
}
