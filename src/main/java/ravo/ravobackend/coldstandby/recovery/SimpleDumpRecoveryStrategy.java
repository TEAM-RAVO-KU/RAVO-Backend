package ravo.ravobackend.coldstandby.recovery;

import org.springframework.stereotype.Component;
import ravo.ravobackend.coldstandby.DatabaseInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class SimpleDumpRecoveryStrategy implements DatabaseRecoveryStrategy {


    @Override
    public void recovery(DatabaseInfo dbInfo, Path dumpFile) throws Exception {

        List<String> command = new ArrayList<>();
        command.add("mysql");
        command.add("-h");
        command.add(dbInfo.getHost());
        command.add("-P");
        command.add(dbInfo.getPort());
        command.add("-u");
        command.add(dbInfo.getUsername());
        command.add("--password=" + dbInfo.getPassword());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectInput(dumpFile.toFile());  // SQL 파일을 stdin으로 전달
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process proc = pb.start();
        int exitCode = proc.waitFor();
        if (exitCode != 0) {
            throw new IOException("MySQL 복구 실패, 종료 코드: " + exitCode);
        }
    }
}
