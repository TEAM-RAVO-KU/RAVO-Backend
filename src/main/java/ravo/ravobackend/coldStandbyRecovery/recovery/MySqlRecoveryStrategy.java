package ravo.ravobackend.coldStandbyRecovery.recovery;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ravo.ravobackend.global.domain.DatabaseProperties;
import ravo.ravobackend.global.util.CommandRequest;
import ravo.ravobackend.global.util.ShellCommandExecutor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MySqlRecoveryStrategy implements RecoveryStrategy {

    private final ShellCommandExecutor shellCommandExecutor;

    @Override
    public boolean support(String driverClassName) {
        return driverClassName.toLowerCase().contains("mysql");
    }

    @Override
    public void recover(DatabaseProperties props, Path dumpFile) throws Exception {
        List<String> cmd = buildMysqlCommand(props);
        Map<String, String> env = new HashMap<>();
        env.put("MYSQL_PWD", props.getPassword()); // 비밀번호를 환경변수로 전달

        log.info("Starting MySQL recovery from: {}", dumpFile);

        shellCommandExecutor.execute(CommandRequest.builder()
                .command(cmd)
                .environmentVariables(env)
                .inputFile(dumpFile.toFile())
                .build());

        log.info("MySQL recovery completed successfully from {}", dumpFile);
    }

    private List<String> buildMysqlCommand(DatabaseProperties props) {
        List<String> cmd = new ArrayList<>();
        cmd.add("mysql");
        cmd.add("-h"); cmd.add(props.getHost());
        cmd.add("-P"); cmd.add(props.getPort());
        cmd.add("-u"); cmd.add(props.getUsername());
        cmd.add(props.getDatabase());   // 복구할 DB 이름
        return cmd;
    }
}
