package ravo.ravobackend.coldStandbyBackup.backup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import ravo.ravobackend.coldStandbyBackup.domain.BackupTarget;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class MySqlBackupStrategyTest {

    @Autowired
    private MySqlBackupStrategy strategy;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.active.jdbc-url}")
    private String jdbcUrl;

    @Value("${spring.datasource.active.username}")
    private String username;

    @Value("${spring.datasource.active.password}")
    private String password;

    @Value("${spring.datasource.active.driver-class-name}")
    private String driverClassName;

    @Value("${backup.output-dir}")
    private String outputDir;

    @Test
    @DisplayName("MySQL 덤프 백업 시, 스키마 생성 및 더미 데이터까지 포함된 SQL 파일이 만들어져야 한다")
    void backupTest() throws Exception {
        //given
        /**
         * 트랜잭션을 지원하는 테스트용 테이블 생성 (InnoDB 엔진 사용)
         */
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "  id INT PRIMARY KEY," +
                        "  name VARCHAR(100) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS orders (" +
                        "  id INT PRIMARY KEY," +
                        "  user_id INT NOT NULL," +
                        "  order_date DATETIME NOT NULL," +
                        "  FOREIGN KEY (user_id) REFERENCES users(id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        jdbcTemplate.execute(
                "CREATE TABLE IF NOT EXISTS order_item (" +
                        "  id INT PRIMARY KEY," +
                        "  order_id INT NOT NULL," +
                        "  product_name VARCHAR(200) NOT NULL," +
                        "  quantity INT NOT NULL," +
                        "  FOREIGN KEY (order_id) REFERENCES orders(id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("TRUNCATE TABLE order_item");
        jdbcTemplate.execute("TRUNCATE TABLE orders");
        jdbcTemplate.execute("TRUNCATE TABLE users");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        // users 테이블에 사용자 2명
        jdbcTemplate.update("INSERT INTO users(id, name) VALUES (?, ?)", 1, "Alice");
        jdbcTemplate.update("INSERT INTO users(id, name) VALUES (?, ?)", 2, "Bob");

        // orders 테이블에 각 사용자별 주문
        jdbcTemplate.update("INSERT INTO orders(id, user_id, order_date) VALUES (?, ?, NOW())", 1, 1);
        jdbcTemplate.update("INSERT INTO orders(id, user_id, order_date) VALUES (?, ?, NOW())", 2, 2);

        // order_item 테이블에 주문별 상품 아이템
        jdbcTemplate.update(
                "INSERT INTO order_item(id, order_id, product_name, quantity) VALUES (?, ?, ?, ?)",
                1, 1, "ProductA", 2
        );
        jdbcTemplate.update(
                "INSERT INTO order_item(id, order_id, product_name, quantity) VALUES (?, ?, ?, ?)",
                2, 1, "ProductB", 3
        );
        jdbcTemplate.update(
                "INSERT INTO order_item(id, order_id, product_name, quantity) VALUES (?, ?, ?, ?)",
                3, 2, "ProductC", 1
        );

        BackupTarget db = strategy.buildBackupTarget(jdbcUrl, username, password, driverClassName);
        Path backupDir = Paths.get(outputDir);

        //when
        strategy.backup(db, backupDir);

        //then
        File dir = backupDir.toFile();
        assertTrue(dir.exists() && dir.isDirectory(), "백업 디렉토리가 존재해야 합니다.");

        File[] dumps = dir.listFiles((d, name) ->
                name.startsWith(db.getDatabaseName()) && name.endsWith(".sql")
        );
        assertNotNull(dumps, "덤프 파일 배열이 null 이면 안 됩니다.");
        assertTrue(dumps.length > 0, "덤프 파일이 하나 이상 생성되어야 합니다.");

        File latest = dumps[dumps.length - 1];
        assertTrue(latest.length() > 0, "덤프 파일이 비어 있으면 안 됩니다.");
    }
}
