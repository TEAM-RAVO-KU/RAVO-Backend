package ravo.ravobackend.coldStandbyRecovery.recovery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldStandbyRecovery.domain.RecoveryTarget;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
@Slf4j
public class DumpRecoveryTasklet implements Tasklet {

    @Value("${backup.output-dir}")
    private String backupDir;

    @Autowired
    @Qualifier("activeDataSourceProperties")
    private DataSourceProperties dataSourceProperties;

    private final RecoveryStrategyFactory factory;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // 1) 파라미터에서 dumpFile 이름 추출
        Object dumpParam = chunkContext.getStepContext()
                .getJobParameters()
                .get("dumpFile");
        if (dumpParam == null) {
            throw new IllegalArgumentException("Missing job parameter 'dumpFile'");
        }
        String dumpFile = dumpParam.toString();
        if (dumpFile == null || dumpFile.isBlank()) {
            throw new IllegalArgumentException("Missing job parameter 'dumpFile'");
        }

        // 2) 덤프 파일 경로 생성 및 존재 확인
        Path dumpPath = Paths.get(backupDir).resolve(dumpFile);
        if (!Files.exists(dumpPath)) {
            throw new FileNotFoundException("SQL dump file not found: " + dumpPath);
        }
        log.info("Starting recovery from dump file: {}", dumpPath);

        // 3) 전략 결정 및 RecoveryTarget 생성
        RecoveryStrategy strategy = factory.getRecoveryStrategy(dataSourceProperties.getDriverClassName());
        RecoveryTarget target = strategy.buildRecoveryTarget(dataSourceProperties);

        // 4) 복구 실행
        strategy.recover(target, dumpPath);
        log.info("Recovery completed successfully for database: {}", target.getDatabaseName());

        return RepeatStatus.FINISHED;
    }
}
