package ravo.ravobackend.recovery.manual.config;

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
import ravo.ravobackend.recovery.manual.tasklet.DumpRecoveryTasklet;

@Configuration
@RequiredArgsConstructor
public class ManualRecoveryConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job manualRecoveryJob(Step dumpRecoveryStep) {
        return new JobBuilder("manualRecoveryJob", jobRepository)
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
