package ravo.ravobackend.coldstandby.backup;

import org.springframework.stereotype.Component;
import ravo.ravobackend.coldstandby.DatabaseInfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class MySqlBackupStrategy implements DatabaseBackupStrategy {

    @Override
    public void backup(DatabaseInfo dbInfo, String backupDir) throws Exception {
        // mysqldump 명령어 리스트 구성
        List<String> command = new ArrayList<>();
        command.add("mysqldump");
        command.add("-h");
        command.add(dbInfo.getHost());
        command.add("-P");
        command.add(dbInfo.getPort());
        command.add("-u");
        command.add(dbInfo.getUsername());
        command.add("--password=" + dbInfo.getPassword());
        command.add("--add-drop-database");
        command.add("--databases");
        command.add(dbInfo.getDatabaseName());
        command.add("--single-transaction");
        command.add("--quick");

        // 출력 파일 경로 및 이름 설정
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String fileName = dbInfo.getDatabaseName() + "_backup_" + timestamp + ".sql";
        File outputFile = new File(backupDir, fileName);

        // 프로세스 실행: mysqldump 결과를 파일로 리다이렉트
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectOutput(outputFile);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        if (pb.start().waitFor() != 0) {
            throw new RuntimeException("mysqldump 실패");
        }
        System.out.println("백업 완료: " + outputFile);
        // exitCode가 0이면 성공적으로 파일 생성
    }
}
