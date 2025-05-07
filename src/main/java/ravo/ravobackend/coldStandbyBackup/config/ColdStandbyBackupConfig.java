package ravo.ravobackend.coldStandbyBackup.config;

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
import ravo.ravobackend.coldStandbyBackup.backup.DumpBackupTasklet;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class ColdStandbyBackupConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DumpBackupTasklet dumpBackupTasklet;

    @Bean
    public Job coldStandbyBackupJob() {
        Step dumpBackupStep = new StepBuilder("dumpBackupStep", jobRepository)
                .tasklet(dumpBackupTasklet, transactionManager)
                .build();

        return new JobBuilder("coldStandbyBackupJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(dumpBackupStep)
                .build();
    }
}
