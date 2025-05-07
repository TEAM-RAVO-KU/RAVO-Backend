package ravo.ravobackend.coldStandbyRecovery.recovery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class RecoveryStrategyFactory {

    private final List<RecoveryStrategy> strategies;

    public RecoveryStrategy getRecoveryStrategy(String driverClassName) {
        return strategies.stream()
                .filter(strategy -> strategy.support(driverClassName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하는 복구 전략이 없습니다 : " + driverClassName));
    }
}
