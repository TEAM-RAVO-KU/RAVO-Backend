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
    private final RestTemplate restTemplate;

    @Value("${application.failover.recover-url}")
    private String recoverUrl;

    @Value("${application.manager.ui-lock-url}")
    private String uiLockUrl;
    @Value("${application.manager.ui-unlock-url}")
    private String uiUnlockUrl;

    @Scheduled(fixedDelay = 1000)
    public void monitorAndTrigger() {
        TargetDB currentTargetDB = statusChecker.fetchStatus();
        log.info("Current DB status: {}, last DB status: {}", currentTargetDB, lastTargetDB);

        if (activeDbHealthChecker.isHealthy() && currentTargetDB == TargetDB.STANDBY) {
            log.info("Binlog Backup Recovery Triggered");
            try {
                // 1) active DB의 UUID 갱신 (direct-active URL 기준)
                try {
                    activeUuidService.refreshActiveUuid();
                    log.info("[RECOVER] Active DB UUID refreshed to {}", activeUuidService.getActiveUuid());
                } catch (Exception uuidEx) {
                    // 안전하게 fail-close: UUID 못 얻으면 /recover 보류해 재시도(다음 스케줄틱에 다시 시도)
                    log.warn("[RECOVER] Failed to refresh active UUID. Will skip /recover this tick.", uuidEx);
                    lastTargetDB = currentTargetDB;
                    return;
                }

                // 2) standby → active 증분 백업을 GTID 동기화까지 반복
                while (true) {
                    GTID currentGtid = gtidService.getCurrentGtidFromStandby();
                    // 마지막 저장된 GTID 조회 (Optional)
                    Optional<GTID> lastSavedOpt = gtidService.getLastSavedGtid(TargetDB.STANDBY);
                    // 범위 계산
                    Optional<GTID> gtidRangeOpt = gtidService.calculateGtidRange(currentGtid, lastSavedOpt.orElse(null));
                    if (gtidRangeOpt.isEmpty()) {
                        break;   // 차이 없으면 종료
                    }
                    //recovery 실행
                    long ts = System.currentTimeMillis();
                    JobParameters jobParameters = new JobParametersBuilder().addLong("ts", ts).toJobParameters();
                    jobLauncher.run(binlogBackupRecoveryJob, jobParameters);
                }

                // 3) FE 통신 (DB watcher 변경 직전 FE 비활성화)
                // TODO : ravo-backend 가 client 와 통신하는게 어색한 듯. 논의 필요
                try {
                    restTemplate.postForObject(uiLockUrl, null, Void.class);
                    log.info("[UI-LOCK] FE locked for recovery transition.");
                } catch (Exception ex) {
                    log.warn("[UI-LOCK] Failed to lock FE UI: {}", ex.getMessage());
                }

                // 4) failover watcher 상태 Active로 변경 & Debezium 재기동 트리거
                try {
                    restTemplate.getForObject(recoverUrl, Void.class);
                    log.info("[RECOVER] Recover endpoint called successfully.");
                } catch (Exception ex) {
                    log.error("[RECOVER] Failed to call recover endpoint", ex);
                }

                // 5) active 로의 전환이 확인되면 FE 와 통신
                if (waitUntilActive(5000L, 500L)) {
                    log.info("[RECOVER] Active DB status confirmed. Unlocking FE UI...");

                    try {
                        restTemplate.postForObject(uiUnlockUrl, null, Void.class);
                        log.info("[UI-UNLOCK] FE unlocked successfully.");
                    } catch (Exception ex) {
                        log.warn("[UI-UNLOCK] Failed to unlock FE UI: {}", ex.getMessage());
                    }
                } else {
                    log.warn("[RECOVER] Active DB not confirmed within timeout (10s).");

                    // 타임 아웃 발생 시, FE 잠금 해제
                    restTemplate.postForObject(uiUnlockUrl, null, Void.class);
                }
            } catch (Exception e) {
                log.error("Failed to run binlog backup recovery job", e);
            }
        }

        lastTargetDB = currentTargetDB;
    }

    /**
     * /status API 를 폴링하여 ACTIVE 상태가 될 때까지 기다림
     */
    private boolean waitUntilActive(long timeoutMs, long intervalMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                TargetDB state = statusChecker.fetchStatus();
                if (state == TargetDB.ACTIVE) return true;
            } catch (Exception e) {
                log.warn("[WAIT] Status check failed: {}", e.getMessage());
            }
            try {
                Thread.sleep(intervalMs);
            } catch (InterruptedException ignored) {}
        }
        return false;
    }
}
