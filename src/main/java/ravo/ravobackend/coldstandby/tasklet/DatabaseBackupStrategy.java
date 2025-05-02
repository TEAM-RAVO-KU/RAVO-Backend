package ravo.ravobackend.coldstandby.tasklet;

public interface DatabaseBackupStrategy {

    void backup(DatabaseInfo dbInfo, String backupDir) throws Exception;
}
