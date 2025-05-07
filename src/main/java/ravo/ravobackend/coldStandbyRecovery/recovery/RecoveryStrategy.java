package ravo.ravobackend.coldStandbyRecovery.recovery;

import ravo.ravobackend.coldStandbyRecovery.domain.RecoveryTarget;

import java.nio.file.Path;

public interface RecoveryStrategy {

    boolean support(String driverClassName);

    RecoveryTarget buildRecoveryTarget(String jdbcUrl, String username, String password, String driverClassName);

    void recover(RecoveryTarget recoveryTarget, Path dumpFile) throws Exception;
}
