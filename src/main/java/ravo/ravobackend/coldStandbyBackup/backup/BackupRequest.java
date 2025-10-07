package ravo.ravobackend.coldStandbyBackup.backup;

import lombok.Builder;
import lombok.Getter;
import ravo.ravobackend.global.domain.DatabaseProperties;

import java.nio.file.Path;

@Getter
@Builder
public class BackupRequest {
    private final DatabaseProperties props;
    private final Path backupDir;
    private final String gtidRange;
    private final String firstBinlogFile;
}
