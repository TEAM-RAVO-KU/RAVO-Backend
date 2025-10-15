package ravo.ravobackend.coldStandbyBackup.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ravo.ravobackend.global.domain.DatabaseProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

import static ravo.ravobackend.global.constants.JobExecutionContextKeys.BACKUP_OUT_FILE;
import static ravo.ravobackend.global.constants.JobExecutionContextKeys.TARGET_DATABASE_PROPERTIES;

@Slf4j
@Component
public class DirectoryInitializerTasklet implements Tasklet {

    @Value("${backup.output-dir}")
    private String outputDir;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        ExecutionContext jobContext = contribution.getStepExecution().getJobExecution().getExecutionContext();
        DatabaseProperties props = (DatabaseProperties) jobContext.get(TARGET_DATABASE_PROPERTIES);

        log.info("[DirectoryInitializerTasklet] props: {}", props);

        Path dumpPath = Paths.get(outputDir);
        Files.createDirectories(dumpPath); // NIO 기반, 예외 발생 시 IOException

        String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Path backupFilePath = dumpPath.resolve(props.getDatabase() + "_" + ts);

        // ExecutionContext에는 문자열로 저장
        jobContext.putString(BACKUP_OUT_FILE, backupFilePath.toAbsolutePath().toString());

        log.info("[DirectoryInitializerTasklet] finished, backupFilePath={}", backupFilePath);
        return RepeatStatus.FINISHED;
    }

}
