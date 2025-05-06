package ravo.ravobackend.global;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.StepScopeTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FullBackupTaskletTest {

    @Autowired
    private FullBackupTasklet fullBackupTasklet;

    @Autowired
    @Qualifier("activeDataSource")
    private DataSource activeDataSource;

    @Autowired
    @Qualifier("standbyDataSource")
    private DataSource standbyDataSource;

    private JdbcTemplate liveJdbcTemplate;
    private JdbcTemplate standbyJdbcTemplate;

    @BeforeEach
    void setUp() {
        liveJdbcTemplate = new JdbcTemplate(activeDataSource);
        standbyJdbcTemplate = new JdbcTemplate(standbyDataSource);
    }

    @Test
    @DisplayName("live DB의 특정 테이블의 모든 데이터를 standby DB에 백업한다.")
    void full_backup_tasklet_test() throws Exception {
        //given
        // live DB: 기존 TEST_TABLE 제거 후 새로 생성
        liveJdbcTemplate.execute("DROP TABLE IF EXISTS TEST_TABLE");
        liveJdbcTemplate.execute("CREATE TABLE TEST_TABLE (ID INT PRIMARY KEY, DATA_FIELD VARCHAR(255))");
        // 테스트 데이터 삽입
        liveJdbcTemplate.execute("INSERT INTO TEST_TABLE (ID, DATA_FIELD) VALUES (1, 'Test1')");
        liveJdbcTemplate.execute("INSERT INTO TEST_TABLE (ID, DATA_FIELD) VALUES (2, 'Test2')");

        // standby DB: 기존 TEST_TABLE 제거 후 새로 생성 (백업용으로 데이터는 비워둠)
        standbyJdbcTemplate.execute("DROP TABLE IF EXISTS TEST_TABLE");
        standbyJdbcTemplate.execute("CREATE TABLE TEST_TABLE (ID INT PRIMARY KEY, DATA_FIELD VARCHAR(255))");

        // JobParameters 생성 & 임시 JobExecution 및 StepExecution 생성
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("tableName", "TEST_TABLE")
                .toJobParameters();

        JobInstance jobInstance = new JobInstance(1L, "testJob");
        JobExecution jobExecution = new JobExecution(jobInstance, jobParameters);
        StepExecution stepExecution = new StepExecution("fullBackupStep", jobExecution);

        //when
        RepeatStatus status = StepScopeTestUtils.doInStepScope(stepExecution, () -> fullBackupTasklet.execute(null, null));

        //then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        // standby DB에 데이터가 복사되었는지 확인 -> test
        Integer count = standbyJdbcTemplate.queryForObject("SELECT COUNT(*) FROM TEST_TABLE", Integer.class);
        assertThat(count).isEqualTo(2);

        // 백업한 데이터의 내용 검증
        Map<String, Object> row1 = standbyJdbcTemplate.queryForMap("SELECT * FROM TEST_TABLE WHERE ID = 1");
        Map<String, Object> row2 = standbyJdbcTemplate.queryForMap("SELECT * FROM TEST_TABLE WHERE ID = 2");

        assertThat(row1.get("DATA_FIELD")).isEqualTo("Test1");
        assertThat(row2.get("DATA_FIELD")).isEqualTo("Test2");
    }


}