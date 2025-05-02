package ravo.ravobackend.coldstandby.recovery;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldstandby.DatabaseInfo;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class DumpRecoveryTasklet implements Tasklet {

    @Value("${backup.output-dir}")
    private String outDir;
    @Value("${spring.datasource.live.jdbc-url}")
    private String url;
    @Value("${spring.datasource.live.username}")
    private String username;
    @Value("${spring.datasource.live.password}")
    private String password;
    @Value("${spring.datasource.live.driver-class-name}")
    private String driver;

    private final DatabaseRecoveryStrategyFactory databaseRecoveryStrategyFactory;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        DatabaseRecoveryStrategy strategy = databaseRecoveryStrategyFactory.getStrategy(driver);

        DatabaseInfo dbInfo = DatabaseInfo.from(url, username, password);
        Path dir = Paths.get(outDir);
        //가장 최근에 업데이트 된 sql dump를 찾는다.
        Path latest = Files.list(dir)
                .filter(p -> p.getFileName().toString().startsWith("active_db_backup_"))
                .sorted((p1, p2) -> Long.compare(p2.toFile().lastModified(), p1.toFile().lastModified()))
                .findFirst()
                .orElseThrow(() -> new FileNotFoundException("백업 파일 없음"));
        strategy.recovery(dbInfo, latest);

        return RepeatStatus.FINISHED;
    }
}
