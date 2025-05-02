package ravo.ravobackend.coldstandby.recovery;

import ravo.ravobackend.coldstandby.DatabaseInfo;

import java.nio.file.Path;

public interface DatabaseRecoveryStrategy {

    void recovery(DatabaseInfo databaseInfo, Path dumpFile) throws Exception;
}
