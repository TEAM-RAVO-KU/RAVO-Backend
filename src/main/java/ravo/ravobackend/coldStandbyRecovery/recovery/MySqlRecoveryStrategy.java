package ravo.ravobackend.coldStandbyRecovery.recovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ravo.ravobackend.global.domain.DatabaseProperties;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class MySqlRecoveryStrategy implements RecoveryStrategy {

    @Override
    public boolean support(String driverClassName) {
        return driverClassName.toLowerCase().contains("mysql");
    }

    @Override
    public void recover(DatabaseProperties props, Path dumpFile) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("mysql");
        cmd.add("-h"); cmd.add(props.getHost());
        cmd.add("-P"); cmd.add(props.getPort());
        cmd.add("-u"); cmd.add(props.getUsername());
        cmd.add("--password=" + props.getPassword());
        cmd.add(props.getDatabase());   // 복구할 DB 이름

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectInput(dumpFile.toFile());
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        int exitCode = pb.start().waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("mysql 복구 실패, exitCode=" + exitCode);
        }

        log.info("Recovery completed from {}", dumpFile);
    }
}
