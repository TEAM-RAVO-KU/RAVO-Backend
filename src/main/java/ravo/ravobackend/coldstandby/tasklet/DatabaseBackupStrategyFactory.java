package ravo.ravobackend.coldstandby.tasklet;

import org.springframework.stereotype.Component;

@Component
public class DatabaseBackupStrategyFactory {
    // 각 전략을 주입받습니다.
    private final MySqlBackupStrategy mySqlStrategy;
    // 필요 시 OracleBackupStrategy 등 추가

    public DatabaseBackupStrategyFactory(MySqlBackupStrategy mySql) {
        this.mySqlStrategy = mySql;
    }

    public DatabaseBackupStrategy getStrategy(String driverClassName) {
        if (driverClassName.contains("mysql")) {
            return mySqlStrategy;
        }
        else {
            throw new IllegalArgumentException("Unsupported DB type for driver: " + driverClassName);
        }
    }
}
