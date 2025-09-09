package ravo.ravobackend.coldStandbyRecovery.recovery;

import ravo.ravobackend.global.DatabaseProperties;

import java.nio.file.Path;

public interface RecoveryStrategy {

    boolean support(String driverClassName);

    void recover(DatabaseProperties databaseProperties, Path dumpFile) throws Exception;
}
