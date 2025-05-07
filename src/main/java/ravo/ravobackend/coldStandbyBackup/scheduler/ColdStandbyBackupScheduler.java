package ravo.ravobackend.coldStandbyBackup.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class ColdStandbyBackupScheduler {

    private final JobLauncher jobLauncher;
    private final Job coldStandbyBackupJob;

    @Scheduled(cron = "${backup.cron}")
    public void scheduleBackup() throws Exception {
        long ts = System.currentTimeMillis();
        jobLauncher.run(coldStandbyBackupJob,
                new JobParametersBuilder()
                        .addLong("ts", ts)
                        .toJobParameters()
        );
    }
}
