package ravo.ravobackend.coldstandby.backup;

import org.springframework.stereotype.Component;

@Component
public class DatabaseBackupStrategyFactory {

    private final MySqlBackupStrategy mySqlStrategy;

    public DatabaseBackupStrategyFactory(MySqlBackupStrategy mySql) {
        this.mySqlStrategy = mySql;
    }

    public DatabaseBackupStrategy getStrategy(String driverClassName) {
        if (driverClassName.contains("mysql")) {
            return mySqlStrategy;
        } else {
            throw new IllegalArgumentException("Unsupported DB type for driver: " + driverClassName);
        }
    }
}
