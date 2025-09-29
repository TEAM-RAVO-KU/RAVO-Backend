package ravo.ravobackend.coldStandbyBackup.backup;

import ravo.ravobackend.global.constants.BackupType;
import ravo.ravobackend.global.domain.DatabaseProperties;

import java.nio.file.Path;

public interface BackupStrategy {

    boolean support(BackupType type);

    void backup(BackupRequest request) throws Exception;
}
