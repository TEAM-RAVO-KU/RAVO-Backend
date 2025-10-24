package ravo.ravobackend.liveSync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActiveUuidService {

    private final @Qualifier("activeJdbcTemplate") JdbcTemplate activeJdbcTemplate;

    private final AtomicReference<String> cachedActiveUuid = new AtomicReference<>(null);

    public String refreshActiveUuid() {
        String uuid = activeJdbcTemplate.queryForObject(
                "SELECT @@server_uuid", String.class
        );

        cachedActiveUuid.set(uuid);     // 캐시 갱신
        log.info("Active DB UUID refreshed: {}", uuid);
        return uuid;
    }

    public String getActiveUuid() {
        String uuid = cachedActiveUuid.get();
        if (uuid != null) return uuid;
        return refreshActiveUuid();
    }
}
