package ravo.ravobackend.coldStandbyBackup.backup;

import ravo.ravobackend.coldStandbyBackup.domain.BackupTarget;

import java.nio.file.Path;

public interface BackupStrategy {

    boolean support(String driverClassName);

    BackupTarget buildBackupTarget(String jdbcUrl, String username, String password, String driverClassName);

    void backup(BackupTarget backupTarget, Path backupDir) throws Exception;
}
