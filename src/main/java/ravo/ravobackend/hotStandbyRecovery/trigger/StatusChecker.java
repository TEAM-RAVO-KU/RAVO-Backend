package ravo.ravobackend.hotStandbyRecovery.trigger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import ravo.ravobackend.global.constants.TargetDB;


@Slf4j
@Component
@RequiredArgsConstructor
public class StatusChecker {

    @Value("${application.failover.status-url}")
    private String statusUrl;

    private final RestTemplate restTemplate;

    /**
     * Failover Manager의 /status API를 호출하여 watcher_state 기반으로 역할 판단
     */
    public TargetDB fetchStatus() {
        try {
            StatusResponse response =
                    restTemplate.getForObject(statusUrl, StatusResponse.class);

            if (response == null || response.getWatcher_state() == null) {
                log.warn("StatusChecker: watcher_state 응답이 없습니다.");
                return TargetDB.UNKNOWN;
            }

            String state = response.getWatcher_state().trim().toUpperCase();

            return switch (state) {
                case "ACTIVE" -> TargetDB.ACTIVE;
                case "STANDBY" -> TargetDB.STANDBY;
                default -> TargetDB.UNKNOWN;
            };

        } catch (RestClientException e) {
            log.error("StatusChecker: 상태 조회 실패 - {}", e.getMessage());
            return TargetDB.UNKNOWN;
        }
    }
}
