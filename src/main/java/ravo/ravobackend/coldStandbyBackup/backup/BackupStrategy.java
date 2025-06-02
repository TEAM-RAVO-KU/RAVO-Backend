package ravo.ravobackend.coldStandbyBackup.backup;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import ravo.ravobackend.coldStandbyBackup.domain.BackupTarget;

import java.nio.file.Path;

public interface BackupStrategy {

    boolean support(String driverClassName);

    BackupTarget buildBackupTarget(DataSourceProperties dataSourceProperties);

    void backup(BackupTarget backupTarget, Path backupDir) throws Exception;
}
