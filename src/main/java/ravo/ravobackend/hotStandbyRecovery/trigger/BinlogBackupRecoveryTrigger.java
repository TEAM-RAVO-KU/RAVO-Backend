package ravo.ravobackend.hotStandbyRecovery.trigger;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.GTID;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.service.GtidService;
import ravo.ravobackend.global.constants.TargetDB;
import ravo.ravobackend.legacy.hotStandbyRecovery.ActiveDbHealthChecker;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinlogBackupRecoveryTrigger {

    private final JobLauncher jobLauncher;
    private final Job binlogBackupRecoveryJob;
    private final StatusChecker statusChecker;
    private final ActiveDbHealthChecker activeDbHealthChecker;
    private static TargetDB lastTargetDB;
    private final RestTemplate restTemplate = new RestTemplate();
    private final GtidService gtidService;
    @Value("${application.failover.recover-url}")
    private String recoverUrl;

    @Scheduled(fixedDelay = 1000)
    public void monitorAndTrigger() {
        TargetDB currentTargetDB = statusChecker.fetchStatus();
        log.info("Current DB status: {}, last DB status: {}", currentTargetDB, lastTargetDB);
        if(activeDbHealthChecker.isHealthy() && currentTargetDB == TargetDB.STANDBY) {
            log.info("Binlog Backup Recovery Triggered");
            try {
                while(true) {
                    GTID currentGtid = gtidService.getCurrentGtidFromStandby();
                    // 2. 마지막 저장된 GTID 조회 (Optional)
                    Optional<GTID> lastSavedOpt = gtidService.getLastSavedGtid(TargetDB.STANDBY);
                    // 3. 범위 계산
                    Optional<GTID> gtidRangeOpt = gtidService.calculateGtidRange(currentGtid, lastSavedOpt.orElse(null));
                    if (gtidRangeOpt.isEmpty()) {
                        // 차이 없으면 종료
                        break;
                    }
                    //recovery 실행
                    long ts = System.currentTimeMillis();
                    JobParameters jobParameters = new JobParametersBuilder().addLong("ts", ts).toJobParameters();
                    jobLauncher.run(binlogBackupRecoveryJob, jobParameters);
                }

                //failover watcher 상태 Active로 변경
                restTemplate.getForObject(recoverUrl, Void.class);
            } catch (Exception e) {
                log.error("Failed to run binlog backup recovery job", e);
            }
        }
        lastTargetDB = currentTargetDB;
    }
}
