package ravo.ravobackend.coldStandbyBackup.backup;

import ravo.ravobackend.global.constants.BackupType;


public interface BackupStrategy {

    boolean support(BackupType type);

    void backup(BackupRequest request) throws Exception;
}
