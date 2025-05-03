package ravo.ravobackend.coldstandby.schedular;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ColdStandbyBackupJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job coldStandbyBackupJob;

    @Scheduled(cron = "${backup.cron}")
    public void runBackupJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("ts", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(coldStandbyBackupJob, jobParameters);
    }
}