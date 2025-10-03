package ravo.ravobackend.coldStandbyBackup.backup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.MySqlBinlogBackupStrategy;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.GTID;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.service.BinlogQueryService;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.service.GtidService;
import ravo.ravobackend.coldStandbyBackup.backup.dump.MySqlDumpBackupStrategy;
import ravo.ravobackend.global.constants.TargetDB;
import ravo.ravobackend.global.domain.DatabaseProperties;

import java.io.File;
import java.nio.file.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MySqlBackupTest {

    @Autowired
    private MySqlDumpBackupStrategy dumpBackupStrategy;

    @Autowired
    private MySqlBinlogBackupStrategy binlogBackupStrategy;

    @Autowired
    @Qualifier("standbyJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @Value("${backup.output-dir}")
    private Path outputDir;

    @Autowired
    private DatabaseProperties standbyDatabaseProperties;

    @Autowired
    private GtidService gtidService;

    @Autowired
    private BinlogQueryService binlogQueryService;

    @BeforeEach
    void setUp() throws Exception {
        // 1) 백업 폴더를 깨끗하게 초기화
        if (Files.exists(outputDir)) {
            Files.walk(outputDir)
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(outputDir);

        // 2) 외래 키 제약 해제 후 이전 테이블 삭제
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.execute("DROP TABLE IF EXISTS order_item");
        jdbcTemplate.execute("DROP TABLE IF EXISTS orders");
        jdbcTemplate.execute("DROP TABLE IF EXISTS users");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

        // 3) 새 테이블 생성
        jdbcTemplate.execute(
                "CREATE TABLE users (" +
                        "  id INT PRIMARY KEY, " +
                        "  name VARCHAR(100) NOT NULL" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        jdbcTemplate.execute(
                "CREATE TABLE orders (" +
                        "  id INT PRIMARY KEY, " +
                        "  user_id INT NOT NULL, " +
                        "  order_date DATETIME NOT NULL, " +
                        "  FOREIGN KEY (user_id) REFERENCES users(id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );
        jdbcTemplate.execute(
                "CREATE TABLE order_item (" +
                        "  id INT PRIMARY KEY, " +
                        "  order_id INT NOT NULL, " +
                        "  product_name VARCHAR(200) NOT NULL, " +
                        "  quantity INT NOT NULL, " +
                        "  FOREIGN KEY (order_id) REFERENCES orders(id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
        );

        // 4) 더미 데이터 삽입
        jdbcTemplate.update("INSERT INTO users(id, name) VALUES (?, ?)", 1, "Alice");
        jdbcTemplate.update("INSERT INTO users(id, name) VALUES (?, ?)", 2, "Bob");
        jdbcTemplate.update("INSERT INTO orders(id, user_id, order_date) VALUES (?, ?, NOW())", 1, 1);
        jdbcTemplate.update("INSERT INTO orders(id, user_id, order_date) VALUES (?, ?, NOW())", 2, 2);
        jdbcTemplate.update("INSERT INTO order_item(id, order_id, product_name, quantity) VALUES (?, ?, ?, ?)", 1, 1, "ProductA", 2);
        jdbcTemplate.update("INSERT INTO order_item(id, order_id, product_name, quantity) VALUES (?, ?, ?, ?)", 2, 1, "ProductB", 3);
        jdbcTemplate.update("INSERT INTO order_item(id, order_id, product_name, quantity) VALUES (?, ?, ?, ?)", 3, 2, "ProductC", 1);
    }

    @Test
    @DisplayName("MySQL 덤프 백업 시, 스키마와 더미 데이터가 포함된 SQL 파일이 생성되어야 한다")
    void dumpBackup_should_create_dumpfile() throws Exception {
        // given
        Path outFile = outputDir.resolve("test-dump.sql");
        BackupRequest req = BackupRequest.builder()
                .backupDir(outFile)
                .props(standbyDatabaseProperties)
                .build();

        // when
        dumpBackupStrategy.backup(req);

        // then
        assertTrue(Files.exists(outputDir), "백업 디렉토리가 존재해야 합니다.");
        assertTrue(Files.isDirectory(outputDir), "백업 디렉토리는 디렉토리여야 합니다.");

        assertTrue(Files.exists(outFile), "지정한 파일명이 생성되어야 합니다.");
        assertTrue(Files.isRegularFile(outFile), "생성된 파일은 일반 파일이어야 합니다.");
        assertTrue(Files.size(outFile) > 0, "백업 파일이 비어 있으면 안 됩니다.");
    }


    @Test
    @DisplayName("MySQL binlog 백업 시, 스키마와 더미 데이터가 포함된 SQL 파일이 생성되어야 한다")
    void GTID_backup_should_create_binlog_file() throws Exception {
        //given
        Path outFile = outputDir.resolve("test-binlog.sql");
        GTID currentGtid = gtidService.getCurrentGtidFromStandby();
        String firstBinlogFile = binlogQueryService.getFirstBinlogFile();

        //when
        gtidService.saveGtid(TargetDB.STANDBY, currentGtid);

        jdbcTemplate.update("INSERT INTO users(id, name) VALUES (?, ?)", 3, "new user1");
        jdbcTemplate.update("INSERT INTO users(id, name) VALUES (?, ?)", 4, "new user2");

        //쿼리 실행 후 현재 gtid 다시 조회
        currentGtid = gtidService.getCurrentGtidFromStandby();
        Optional<GTID> lastSavedGtid = gtidService.getLastSavedGtid(TargetDB.STANDBY);

        System.out.println("new current gtid: " + currentGtid.toString());
        GTID gtidRange = gtidService.calculateGtidRange(currentGtid, lastSavedGtid.orElse(null)).orElseThrow();


        BackupRequest req = BackupRequest.builder()
                .backupDir(outFile)
                .gtidRange(gtidRange.toString())
                .props(standbyDatabaseProperties)
                .firstBinlogFile(firstBinlogFile)
                .build();

        binlogBackupStrategy.backup(req);

        //then
        assertTrue(Files.exists(outputDir), "백업 디렉토리가 존재해야 합니다.");
        assertTrue(Files.isDirectory(outputDir), "백업 디렉토리는 디렉토리여야 합니다.");
        assertTrue(Files.exists(outFile), "지정한 파일명이 생성되어야 합니다.");
        assertTrue(Files.isRegularFile(outFile), "생성된 파일은 일반 파일이어야 합니다.");
        assertTrue(Files.size(outFile) > 0, "백업 파일이 비어 있으면 안 됩니다.");

    }
}
