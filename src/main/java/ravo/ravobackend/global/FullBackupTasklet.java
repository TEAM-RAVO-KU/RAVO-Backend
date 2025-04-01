package ravo.ravobackend.global;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@StepScope
public class FullBackupTasklet implements Tasklet {

    private final JdbcTemplate liveJdbcTemplate;
    private final JdbcTemplate standbyJdbcTemplate;

    @Value("#{jobParameters['tableName']}")
    private String tableName;

    public FullBackupTasklet(@Qualifier("liveDataSource") DataSource liveDataSource, @Qualifier("standbyDataSource") DataSource standbyDataSource) {
        this.liveJdbcTemplate = new JdbcTemplate(liveDataSource);
        this.standbyJdbcTemplate = new JdbcTemplate(standbyDataSource);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // 1. tableName validation
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Job parameter 'tableName'은 필수입니다.");
        }

        // 2. live DB에서 대상 테이블의 컬럼 이름 조회
        List<String> columns = liveJdbcTemplate.query("SHOW COLUMNS FROM " + tableName,
                (rs, rowNum) -> rs.getString("Field"));         // Field 컬럼에 각 컬럼의 이름이 저장되어 있다

        // 3. live DB에서 대상 테이블의 모든 데이터 조회
        List<Map<String, Object>> rows = liveJdbcTemplate.queryForList("SELECT * FROM " + tableName);

        if (!rows.isEmpty()) {
            // 4. 동적으로 insert sql문 생성
            String columnList = String.join(", ", columns);     // 현재 테이블의 모든 컬럼 이름들의 List
            String placeholders = columns.stream().map(col -> "?").collect(Collectors.joining(", "));       // 모든 컬럼들을 '?' 로 교체 -> placeholder 역할
            String insertSql = "INSERT INTO " + tableName + " (" + columnList + ") VALUES (" + placeholders + ")";      // 동적으로 생성되는 insert sql문

            /**
             * <위 결과로 생성되는 insertSql>
             * ex) 대상 테이블이 "TEST" 이고, 컬럼이 {"id", "data"] 인 경우
             * -> INSERT INTO TEST (id, data) VALUES (?, ?)
             */

            // 5. 각 행에 대해 standby DB에 insert command 실행
            for (Map<String, Object> row : rows) {
                List<Object> params = new ArrayList<>();
                for (String col : columns) {
                    params.add(row.get(col));
                }
                standbyJdbcTemplate.update(insertSql, params.toArray());
            }
        }

        return RepeatStatus.FINISHED;       // 현재 Tasklet이 모든 작업을 완료했음을 의미 -> 현재 step 종료(= 현재 Tasklet 반복 호출 X)
    }
}
