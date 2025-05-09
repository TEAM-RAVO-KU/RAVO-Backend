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
import ravo.ravobackend.coldStandbyBackup.backup.DumpBackupTasklet;

@Configuration
@RequiredArgsConstructor
public class ColdStandbyBackupConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job coldStandbyBackupJob(Step dumpBackupStep) {
        return new JobBuilder("coldStandbyBackupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(dumpBackupStep)
                .end()
                .build();
    }

    @Bean
    public Step dumpBackupStep(DumpBackupTasklet t) {
        return new StepBuilder("dumpBackupStep", jobRepository)
                .tasklet(t, transactionManager)
                .build();
    }
}
