package ravo.ravobackend.coldStandbyRecovery.recovery;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import ravo.ravobackend.coldStandbyRecovery.domain.RecoveryTarget;

import java.nio.file.Path;

public interface RecoveryStrategy {

    boolean support(String driverClassName);

    RecoveryTarget buildRecoveryTarget(DataSourceProperties dataSourceProperties);

    void recover(RecoveryTarget recoveryTarget, Path dumpFile) throws Exception;
}
