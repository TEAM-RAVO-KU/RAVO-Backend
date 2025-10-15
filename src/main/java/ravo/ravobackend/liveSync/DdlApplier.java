package ravo.ravobackend.liveSync;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DdlApplier {

    private final @Qualifier("standbyJdbcTemplate") JdbcTemplate standbyJdbcTemplate;

    private static final Set<String> ALLOW_PREFIX = Set.of(
            "create table", "alter table", "drop table",
            "create index", "drop index",
            "rename table", "truncate table"
    );

    public void apply(DdlEvent e) {
        String ddl = Optional.ofNullable(e.ddl()).orElse("").trim();
        if (ddl.isEmpty()) return;

        String lower = ddl.toLowerCase(Locale.ROOT);
        boolean allowed = ALLOW_PREFIX.stream().anyMatch(lower::startsWith);
        if (!allowed) { // 일단 무시
            return;
        }

        standbyJdbcTemplate.execute(ddl);
    }
}
