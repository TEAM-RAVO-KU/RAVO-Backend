package ravo.ravobackend.coldstandby.recovery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseRecoveryStrategyFactory {

    private final SimpleDumpRecoveryStrategy simpleDumpRecoveryStrategy;

    public DatabaseRecoveryStrategy getStrategy(String driverClassName) {
        if (driverClassName.contains("mysql")) {
            return simpleDumpRecoveryStrategy;
        } else {
            throw new IllegalArgumentException("Unsupported DB type for driver: " + driverClassName);
        }
    }
}
