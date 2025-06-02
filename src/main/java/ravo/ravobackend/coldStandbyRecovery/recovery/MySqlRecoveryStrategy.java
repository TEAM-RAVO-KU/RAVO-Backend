package ravo.ravobackend.coldStandbyRecovery.recovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldStandbyRecovery.domain.RecoveryTarget;
import ravo.ravobackend.global.util.JdbcUrlParser;

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
    public RecoveryTarget buildRecoveryTarget(DataSourceProperties props) {
        // 1) DataSourceProperties에서 필수 정보 가져오기
        String jdbcUrl = props.getUrl();
        String username = props.getUsername();
        String password = props.getPassword();
        String driverClassName = props.getDriverClassName();

        // 2) JDBC URL 파싱
        JdbcUrlParser.ParsedResult parsed = JdbcUrlParser.parse(jdbcUrl);

        // 3) RecoveryTarget 빌더로 조립
        return RecoveryTarget.builder()
                .host(parsed.getHost())
                .port(parsed.getPort())
                .databaseName(parsed.getDatabaseName())
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
