package ravo.ravobackend.hotStandbyRecovery.trigger;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ravo.ravobackend.global.constants.TargetDB;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinlogBackupRecoveryTrigger {

    private final JobLauncher jobLauncher;
    private final Job binlogBackupRecoveryJob;
    private final StatusChecker statusChecker;
    private static TargetDB lastTargetDB;

    @PostConstruct
    public void init() {
        lastTargetDB = statusChecker.fetchStatus();
    }

    @Scheduled(fixedDelay = 1000)
    public void monitorAndTrigger() {
        TargetDB currentTargetDB = statusChecker.fetchStatus();
        log.info("Current DB status: {}, last DB status: {}", currentTargetDB, lastTargetDB);
        if(lastTargetDB == TargetDB.STANDBY && currentTargetDB == TargetDB.ACTIVE) {
            log.info("Binlog Backup Recovery Triggered");
            try {
                long ts = System.currentTimeMillis();
                JobParameters jobParameters = new JobParametersBuilder().addLong("ts", ts).toJobParameters();

                //recovery 실행
                jobLauncher.run(binlogBackupRecoveryJob, jobParameters);

            } catch (Exception e) {
                log.error("Failed to run binlog backup recovery job", e);
            }
        }
        lastTargetDB = currentTargetDB;
    }
}
