package ravo.ravobackend.recovery.manual.service;

import ravo.ravobackend.global.constants.DatabaseProperties;

import java.nio.file.Path;

public interface RecoveryStrategy {

    boolean support(String driverClassName);

    void recover(DatabaseProperties databaseProperties, Path dumpFile) throws Exception;
}
