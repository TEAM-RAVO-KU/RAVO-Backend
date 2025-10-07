package ravo.ravobackend.coldStandbyBackup.backup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ravo.ravobackend.global.constants.BackupType;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BackupStrategyFactory {

    private final List<BackupStrategy> strategies;

    public BackupStrategy getBackupStrategy(BackupType type) {
        return strategies.stream()
                .filter(strategy -> strategy.support(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하는 백업 전략이 없습니다 : " + type));
    }
}
