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
import ravo.ravobackend.liveSync.ActiveUuidService;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinlogBackupRecoveryTrigger {

    private final JobLauncher jobLauncher;
    private final Job binlogBackupRecoveryJob;
    private final StatusChecker statusChecker;
    private final ActiveDbHealthChecker activeDbHealthChecker;
    private final GtidService gtidService;
    private final ActiveUuidService activeUuidService;

    private static TargetDB lastTargetDB;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${application.failover.recover-url}")
    private String recoverUrl;

    @Scheduled(fixedDelay = 1000)
    public void monitorAndTrigger() {
        TargetDB currentTargetDB = statusChecker.fetchStatus();
        log.info("Current DB status: {}, last DB status: {}", currentTargetDB, lastTargetDB);

        if(activeDbHealthChecker.isHealthy() && currentTargetDB == TargetDB.STANDBY) {
            log.info("Binlog Backup Recovery Triggered");
            try {
                // 1) standby → active 증분 백업을 GTID 동기화까지 반복
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

                // 2) /recover 호출 직전에 active DB의 UUID 갱신 (direct-active URL 기준)
                try {
                    activeUuidService.refreshActiveUuid();
                    log.info("[RECOVER] Active DB UUID refreshed to {}", activeUuidService.getActiveUuid());
                } catch (Exception uuidEx) {
                    // 안전하게 fail-close: UUID 못 얻으면 /recover 보류해 재시도(다음 스케줄틱에 다시 시도)
                    log.warn("[RECOVER] Failed to refresh active UUID. Will skip /recover this tick.", uuidEx);
                    lastTargetDB = currentTargetDB;
                    return;
                }

                // 3) failover watcher 상태 Active로 변경 & Debezium 재기동 트리거
                restTemplate.getForObject(recoverUrl, Void.class);
                log.info("[RECOVER] Recover endpoint called successfully.");
            } catch (Exception e) {
                log.error("Failed to run binlog backup recovery job", e);
            }
        }
        lastTargetDB = currentTargetDB;
    }
}
