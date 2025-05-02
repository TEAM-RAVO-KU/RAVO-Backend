package ravo.ravobackend.coldstandby.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@RequiredArgsConstructor
public class DumpBackupTasklet implements Tasklet {

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

    private final DatabaseBackupStrategyFactory databaseBackupStrategyFactory;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        DatabaseBackupStrategy strategy = databaseBackupStrategyFactory.getStrategy(driver);

        new File(outDir).mkdirs();

        DatabaseInfo dbInfo = DatabaseInfo.from(url, username, password);

        strategy.backup(dbInfo, outDir);

        return RepeatStatus.FINISHED;
    }

}
