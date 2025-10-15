package ravo.ravobackend.legacy.hotStandbyRecovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JdbcActiveDbHealthChecker implements ActiveDbHealthChecker {

    private final JdbcTemplate jdbcTemplate;

    public JdbcActiveDbHealthChecker(@Qualifier("directActiveJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isHealthy() {
        try {
            // Active DB가 살아있는지 간단한 쿼리로 확인
            jdbcTemplate.queryForObject("select 1", Integer.class);
            return true;
        } catch (DataAccessException e) {
            log.debug("Active DB 연결 실패. 다음 스케줄에서 재시도합니다.", e);
            return false;
        }
    }
}
