package ravo.ravobackend.coldStandbyBackup.backup.binlog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.GTID;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.GtidHistory;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.repository.GtidRepository;
import ravo.ravobackend.global.constants.TargetDB;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GtidService {

    private final GtidRepository gtidRepository;
    private final JdbcTemplate standbyJdbcTemplate;

    /**
     * StandbyDB에서 현재 GTID를 조회
     */
    public GTID getCurrentGtidFromStandby() {

        String gtidSet = standbyJdbcTemplate.queryForObject(
                "SELECT @@GLOBAL.gtid_executed",
                String.class
        );

        log.info("Current GTID from Standby DB: {}", gtidSet);
        return GTID.parse(gtidSet);
    }

    /**
     * GTID를 DB에 저장
     */
    @Transactional
    public GtidHistory saveGtid(TargetDB targetDB, GTID gtid) {
        GtidHistory history = GtidHistory.builder()
                .dbName(targetDB.name())
                .gtidSet(gtid.toString())
                .build();

        GtidHistory saved = gtidRepository.save(history);
        log.info("Saved GTID history - DB: {}, GTID: {}", targetDB.name(), gtid);

        return saved;
    }

    /**
     * 마지막으로 저장된 GTID 조회
     */
    public Optional<GTID> getLastSavedGtid(TargetDB targetDB) {
        return gtidRepository.findTop1ByDbNameOrderByCreatedAtDesc(targetDB.name())
                .map(history -> {
                    log.info("Found last saved GTID - DB: {}, GTID: {}", targetDB.name(), history.getGtidSet());
                    return GTID.parse(history.getGtidSet());
                });
    }

    /**
     * 현재 GTID와 저장된 GTID를 비교하여 백업할 범위 계산
     *
     * @param currentGtid 현재 GTID
     * @param lastSavedGtid 마지막 저장된 GTID
     * @return 백업할 GTID 범위 (차이분)
     */
    public Optional<GTID> calculateGtidRange(GTID currentGtid, GTID lastSavedGtid) {
        log.info("[CalculateGtidRange] current GTID: {}, last saved GTID: {}", currentGtid, lastSavedGtid);
        if (lastSavedGtid == null) {
            log.info("No previous GTID found. Will backup all: {}", currentGtid);
            return Optional.of(currentGtid);
        }

        GTID diff = currentGtid.subtract(lastSavedGtid);

        if (diff.isEmpty()) {
            log.info("No new GTID changes detected");
            return Optional.empty();
        }

        log.info("Calculated GTID range for backup - Previous: {}, Current: {}, Diff: {}",
                lastSavedGtid, currentGtid, diff);

        return Optional.of(diff);
    }
}
