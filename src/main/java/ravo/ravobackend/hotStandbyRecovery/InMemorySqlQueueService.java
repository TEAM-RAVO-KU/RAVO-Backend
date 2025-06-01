package ravo.ravobackend.hotStandbyRecovery;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class InMemorySqlQueueService implements SqlQueueService {

    /**
     *  Debezium → Kafka → StandbyConsumer.enqueue()로 들어온 SQL을 보관할 쓰레드-세이프 큐
     */
    @Getter
    private final Queue<String> queue = new ConcurrentLinkedQueue<>();

    private final JdbcTemplate jdbcTemplate;
    private final ActiveDbHealthChecker healthChecker;

    public InMemorySqlQueueService(@Qualifier("activeJdbcTemplate") JdbcTemplate jdbcTemplate, ActiveDbHealthChecker healthChecker) {
        this.jdbcTemplate = jdbcTemplate;
        this.healthChecker = healthChecker;
    }

    @Override
    public void enqueue(String sql) {
        queue.add(sql);
        log.debug("SQL enqueued: {}", sql);
    }

    @Scheduled(fixedDelay = 1000)
    public void flushQueue() {
        // Active DB 헬스체크
        if (!healthChecker.isHealthy()) {
            log.warn("Active DB 비정상 (헬스체크 실패). 다음 스케줄에서 재시도합니다.");
            return;
        }

        // Active DB가 살아있다면, queue에 들어있는 SQL문을 모두 꺼내서 실행
        String sql;
        while ((sql = queue.peek()) != null) {
            try {
                jdbcTemplate.execute(sql);
                log.info("Flushed SQL to Active DB : {}", sql);

                queue.poll();       // Active DB에 성공적으로 반영되면 queue에서 poll
            } catch (DataAccessException e) {
                // SQL 실행 실패 시, 해당 sql문을 재시도
                log.error("Failed to execute SQL on Active DB, will retry later : {}", sql, e);
                break;      // 일단 현재 스케줄러 종료
            }
        }
    }
}
