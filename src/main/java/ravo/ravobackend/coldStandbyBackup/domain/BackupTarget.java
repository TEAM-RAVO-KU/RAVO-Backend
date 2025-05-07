package ravo.ravobackend.coldStandbyBackup.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BackupTarget {

    private final String host;

    private final String port;

    private final String databaseName;

    private final String username;

    private final String password;

    private final String driverClassName;
}
