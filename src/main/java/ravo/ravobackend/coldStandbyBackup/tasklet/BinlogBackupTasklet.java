package ravo.ravobackend.coldStandbyBackup.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldStandbyBackup.backup.BackupRequest;
import ravo.ravobackend.coldStandbyBackup.backup.BackupStrategy;
import ravo.ravobackend.coldStandbyBackup.backup.BackupStrategyFactory;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.GTID;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.service.BinlogQueryService;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.service.GtidService;
import ravo.ravobackend.global.constants.BackupType;
import ravo.ravobackend.global.constants.TargetDB;
import ravo.ravobackend.global.domain.DatabaseProperties;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static ravo.ravobackend.global.constants.JobExecutionContextKeys.BACKUP_OUT_FILE;
import static ravo.ravobackend.global.constants.JobExecutionContextKeys.TARGET_DATABASE_PROPERTIES;


@Slf4j
@Component
@RequiredArgsConstructor
public class BinlogBackupTasklet implements Tasklet {

    private final BackupStrategyFactory factory;
    private final GtidService gtidService;
    private final BinlogQueryService binlogQueryService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ExecutionContext jobContext = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();

        DatabaseProperties props = (DatabaseProperties) jobContext.get(TARGET_DATABASE_PROPERTIES);
        Path backupDir = Paths.get(jobContext.getString(BACKUP_OUT_FILE));


        BackupStrategy backupStrategy = factory.getBackupStrategy(BackupType.MYSQL_BINLOG);

        // 1. Standby DB에서 현재 GTID 조회
        GTID currentGtid = gtidService.getCurrentGtidFromStandby();

        // 2. 마지막 저장된 GTID 조회 (Optional)
        Optional<GTID> lastSavedOpt = gtidService.getLastSavedGtid(TargetDB.STANDBY);

        // 3. 범위 계산
        Optional<GTID> gtidRangeOpt = gtidService.calculateGtidRange(currentGtid, lastSavedOpt.orElse(null));

        if (gtidRangeOpt.isEmpty()) {
            // 차이 없으면 백업 스킵
            log.info("No new GTIDs found. Skipping binlog backup.");
            return RepeatStatus.FINISHED;
        }

        GTID gtidRange = gtidRangeOpt.get();
        String gtidRangeStr = gtidRange.toString();
        backupDir = backupDir.resolve(gtidRangeStr.substring(gtidRangeStr.length()-10) + ".sql");

        //첫번째 binlog 파일명 조회
        String firstBinlogFile = binlogQueryService.getFirstBinlogFile();

        // 4. 백업 요청 객체 생성
        BackupRequest req = BackupRequest.builder()
                .props(props)
                .backupDir(backupDir)
                .gtidRange(gtidRange.toString())
                .firstBinlogFile(firstBinlogFile)
                .build();

        // 5. 백업 실행
        backupStrategy.backup(req);

        // 6. GTID 저장
        gtidService.saveGtid(TargetDB.STANDBY, currentGtid);

        log.info("Binlog backup completed successfully. Range: {}", gtidRange);
        return RepeatStatus.FINISHED;
    }
}
