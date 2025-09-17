package ravo.ravobackend.coldStandbyRecovery.recovery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ravo.ravobackend.global.domain.DatabaseProperties;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MySqlRecoveryStrategyTest {

    @Autowired
    private MySqlRecoveryStrategy strategy;

    @Autowired
    @Qualifier("activeJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("activeDatabaseProperties")
    private DatabaseProperties activeDatabaseProperties;

    @Value("${backup.output-dir}")
    private String dumpDir;

    @TempDir
    Path tempDir;

    @BeforeEach
    void cleanup() {
        // 복구 테스트용 테이블이 남아 있으면 제거
        jdbcTemplate.execute("DROP TABLE IF EXISTS test_recovery");
    }

    @Test
    @DisplayName("특정 덤프 파일로 active DB를 복구할 수 있어야 한다.")
    void recoverTest() throws Exception {
        //given
        Path dumpFile = tempDir.resolve("test_recovery.sql");
        String sql = """
            CREATE TABLE IF NOT EXISTS test_recovery (
              id INT PRIMARY KEY,
              name VARCHAR(50) NOT NULL
            );
            INSERT INTO test_recovery (id, name) VALUES
              (1, 'Alice'),
              (2, 'Bob');
            """;
        Files.writeString(dumpFile, sql);
        assertTrue(Files.exists(dumpFile), "덤프 파일이 생성되어야 합니다.");

        //when
        strategy.recover(activeDatabaseProperties, dumpFile);

        //then
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM test_recovery", Integer.class
        );
        assertEquals(2, count, "레코드가 2건 복구되어야 합니다.");

        String name1 = jdbcTemplate.queryForObject(
                "SELECT name FROM test_recovery WHERE id = 1", String.class
        );
        assertEquals("Alice", name1, "첫 번째 레코드가 'Alice'여야 합니다.");

        String name2 = jdbcTemplate.queryForObject(
                "SELECT name FROM test_recovery WHERE id = 2", String.class
        );
        assertEquals("Bob", name2, "두 번째 레코드가 'Bob'여야 합니다.");
    }
}
