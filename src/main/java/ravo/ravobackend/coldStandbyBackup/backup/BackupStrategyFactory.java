package ravo.ravobackend.coldStandbyBackup.backup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BackupStrategyFactory {

    private final List<BackupStrategy> strategies;

    public BackupStrategy getBackupStrategy(String driverClassName) {
        return strategies.stream()
                .filter(strategy -> strategy.support(driverClassName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하는 백업 전략이 없습니다 : " + driverClassName));
    }
}
