package ravo.ravobackend.coldstandby.backup;

import ravo.ravobackend.coldstandby.DatabaseInfo;

public interface DatabaseBackupStrategy {

    void backup(DatabaseInfo dbInfo, String backupDir) throws Exception;
}
