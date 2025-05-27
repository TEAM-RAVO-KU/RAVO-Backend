package ravo.ravobackend.hotStandbyPoc;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.PreparedStatement;
import java.sql.Statement;

@RestController
public class WriteToStandby {

    private JdbcTemplate jdbcTemplate;

    // standby 전용 JdbcTemplate로 날라가도록 명시
    @Autowired
    public WriteToStandby(@Qualifier("standbyJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/api/test/standby-write")
    public void writeToStandby(@RequestBody StandbyWriteRequest request) {
        String sql = "insert into integrity_data (data) values (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.getData());
            return ps;
        }, keyHolder);
    }
}
