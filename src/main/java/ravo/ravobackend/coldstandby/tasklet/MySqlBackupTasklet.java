package ravo.ravobackend.coldstandby.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class MySqlBackupTasklet implements Tasklet {

    @Value("${backup.mysqldump-exe}")
    private String dumpExe;

    @Value("${backup.output-dir}")
    private String outDir;

    @Value("${spring.datasource.live.jdbc-url}")
    private String url;

    @Value("${spring.datasource.live.username}")
    private String user;

    @Value("${spring.datasource.live.password}")
    private String pass;

    @Override
    public RepeatStatus execute(StepContribution c, ChunkContext ctx) throws Exception {
        // 1) JDBC URL 파싱 → host, port, dbName 추출
        String hostPortDb = url.substring("jdbc:mysql://".length()).split("\\?")[0];
        String host = hostPortDb.split(":")[0];
        String port = hostPortDb.split(":")[1].split("/")[0];
        String db = hostPortDb.substring(hostPortDb.lastIndexOf('/') + 1);

        // 2) 출력 폴더 준비
        new File(outDir).mkdirs();
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String outFile = outDir + File.separator + db + "_backup_" + ts + ".sql";

        // 3) mysqldump 명령어 구성
        ProcessBuilder pb = new ProcessBuilder(
                dumpExe, "-h", host, "-P", port, "-u", user, "-p" + pass, db);
        pb.redirectOutput(new File(outFile));
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        // 4) 실행 및 종료 코드 확인
        if (pb.start().waitFor() != 0) {
            throw new RuntimeException("mysqldump 실패");
        }
        System.out.println("백업 완료: " + outFile);
        return RepeatStatus.FINISHED;
    }
}
