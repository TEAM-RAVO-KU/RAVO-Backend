package ravo.ravobackend.coldStandbyBackup.backup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ravo.ravobackend.global.DatabaseProperties;

import java.io.File;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class MySqlBackupStrategy implements BackupStrategy {

    @Override
    public boolean support(String driverClassName) {
        return driverClassName.toLowerCase().contains("mysql");
    }

    @Override
    public void backup(DatabaseProperties props, Path backupDir) throws Exception{
        // 1) 덤프 파일 저장 디렉터리 준비
        File dir = backupDir.toFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IllegalStateException("백업 디렉터리 생성 실패: " + dir.getAbsolutePath());
        }

        // 2) mysqldump 커맨드 옵션 구성
        List<String> cmd = new ArrayList<>();
        cmd.add("mysqldump");
        cmd.add("-h"); cmd.add(props.getHost());                     // 호스트 지정
        cmd.add("-P"); cmd.add(props.getPort());                     // 포트 지정
        cmd.add("-u"); cmd.add(props.getUsername());                 // 사용자 지정
        cmd.add("--password=" + props.getPassword());                // 비밀번호 지정
        cmd.add("--add-drop-database");                                 // 복원 시 DROP DATABASE 포함
        cmd.add("--databases"); cmd.add(props.getDatabase());    // 덤프할 데이터베이스 이름
        cmd.add("--single-transaction");                                // 인덱스 잠금 최소화
        cmd.add("--quick");                                             // 대용량 테이블도 빠르게 스트리밍

        // 3) 출력 파일명에 타임스탬프 추가
        String ts = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        File outFile = new File(dir, props.getDatabase() + "_cold_" + ts + ".sql");

        // 4) 외부 프로세스 실행 설정
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectOutput(outFile);                                   // stdout → SQL 파일
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);            // stderr → 콘솔

        // 5) 프로세스 실행 및 종료 코드 확인
        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("mysqldump 실패, exitCode=" + exitCode);
        }

        // 6) 로그 출력
        log.info("Cold standby 백업 완료: {}", outFile.getAbsolutePath());
    }
}
