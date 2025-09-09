package ravo.ravobackend.coldStandbyBackup.backup;

import ravo.ravobackend.global.DatabaseProperties;

import java.nio.file.Path;

public interface BackupStrategy {

    boolean support(String driverClassName);

    void backup(DatabaseProperties databaseProperties, Path backupDir) throws Exception;
}
