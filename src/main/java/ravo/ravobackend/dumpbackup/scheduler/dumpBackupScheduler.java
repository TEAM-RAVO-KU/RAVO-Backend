package ravo.ravobackend.dumpbackup.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class dumpBackupScheduler {

    private final JobLauncher jobLauncher;
    private final Job dumpBackupJob;

    @Scheduled(cron = "${backup.cron}")
    public void scheduleBackup() throws Exception {
        long ts = System.currentTimeMillis();
        jobLauncher.run(dumpBackupJob,
                new JobParametersBuilder()
                        .addLong("ts", ts)
                        .toJobParameters()
        );
    }
}
