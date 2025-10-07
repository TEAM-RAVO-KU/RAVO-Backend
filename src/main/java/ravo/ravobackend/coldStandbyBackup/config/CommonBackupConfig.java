package ravo.ravobackend.coldStandbyBackup.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import ravo.ravobackend.coldStandbyBackup.tasklet.DirectoryInitializerTasklet;
import ravo.ravobackend.coldStandbyBackup.tasklet.TargetDatabaseSelectorTasklet;

@Configuration
@RequiredArgsConstructor
public class CommonBackupConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

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
