package ravo.ravobackend.coldStandbyRecovery.recovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldStandbyRecovery.domain.RecoveryTarget;

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
    public RecoveryTarget buildRecoveryTarget(String jdbcUrl, String username, String password, String driverClassName) {
        // MySQL URL: jdbc:mysql://host:port/db?params
        String core = jdbcUrl.substring("jdbc:mysql://".length()).split("\\?")[0];
        String[] parts = core.split("/", 2);
        String[] hp = parts[0].split(":", 2);
        String host = hp[0];
        String port = hp.length > 1 ? hp[1] : "3306";
        String databaseName = parts.length > 1 ? parts[1] : "";

        return RecoveryTarget.builder()
                .host(host)
                .port(port)
                .databaseName(databaseName)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Override
    public void recover(RecoveryTarget recoveryTarget, Path dumpFile) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("mysql");
        cmd.add("-h"); cmd.add(recoveryTarget.getHost());
        cmd.add("-P"); cmd.add(recoveryTarget.getPort());
        cmd.add("-u"); cmd.add(recoveryTarget.getUsername());
        cmd.add("--password=" + recoveryTarget.getPassword());
        cmd.add(recoveryTarget.getDatabaseName());   // 복구할 DB 이름

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
