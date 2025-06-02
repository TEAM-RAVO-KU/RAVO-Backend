package ravo.ravobackend.hotStandbyRecovery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InMemorySqlQueueServiceTest {

    private InMemorySqlQueueService sqlQueueService;
    private JdbcTemplate mockJdbcTemplate;
    private ActiveDbHealthChecker mockHealthChecker;

    @BeforeEach
    void setUp() {
        mockJdbcTemplate = mock(JdbcTemplate.class);
        mockHealthChecker = mock(ActiveDbHealthChecker.class);
        sqlQueueService = new InMemorySqlQueueService(mockJdbcTemplate, mockHealthChecker);
    }

    @Test
    @DisplayName("String sql문을 내부 Queue에 저장한다.")
    void enqueue() throws Exception {
        // given
        String sql1 = "INSERT INTO test (id) VALUES (1)";
        String sql2 = "UPDATE test SET id=2 WHERE id=1";

        // when
        sqlQueueService.enqueue(sql1);
        sqlQueueService.enqueue(sql2);

        // then
        assertThat(sqlQueueService.getQueue()).containsExactly(sql1, sql2);
    }

    @Test
    @DisplayName("active DB가 on 상태일 경우, 내부 Queue의 모든 sql문들이 active DB에 반영되어야 한다.")
    void flushQueue_success_when_active_on() throws Exception {
        when(mockHealthChecker.isHealthy()).thenReturn(true);       // active DB : on
        doNothing().when(mockJdbcTemplate).execute(anyString());

        String sqlA = "SQL_A";
        String sqlB = "SQL_B";
        String sqlC = "SQL_C";
        sqlQueueService.enqueue(sqlA);
        sqlQueueService.enqueue(sqlB);
        sqlQueueService.enqueue(sqlC);

        // when
        sqlQueueService.flushQueue();

        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockJdbcTemplate, times(3)).execute(captor.capture());
        assertThat(captor.getAllValues()).containsExactly(sqlA, sqlB, sqlC);        // a->b->c 순서로 execute 메서드 호출하는지 확인

        assertThat(sqlQueueService.getQueue()).isEmpty();       // flushQueue 이후 Queue 내부는 empty 상태
    }

    @Test
    @DisplayName("active DB가 off 상태일 경우, flushQueue 메서드는 동작하면 안된다.")
    void flushQueue_not_success_when_active_off() throws Exception {
        // given
        when(mockHealthChecker.isHealthy()).thenReturn(false);      // active DB : off

        String sql1 = "SQL1";
        String sql2 = "SQL2";
        sqlQueueService.enqueue(sql1);
        sqlQueueService.enqueue(sql2);

        // when
        sqlQueueService.flushQueue();

        // then
        verify(mockJdbcTemplate, never()).execute(anyString());
        assertThat(sqlQueueService.getQueue()).containsExactly(sql1, sql2);
    }
}