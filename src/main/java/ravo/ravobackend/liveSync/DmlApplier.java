package ravo.ravobackend.liveSync;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DmlApplier {

    private final @Qualifier("standbyJdbcTemplate") JdbcTemplate standbyJdbcTemplate;

    public void apply(DmlEvent e) {
        switch (e.op()) {
            case "c" -> insert(e);
            case "u" -> update(e);
            case "d" -> delete(e);
            default -> {}
        }
    }

    private void insert(DmlEvent e) {
        Map<String, Object> cols = toMap(e.after());

        String table = e.table();
        String columns = String.join(",", cols.keySet());
        String placeholders = cols.keySet().stream()
                .map(k -> "?")
                .collect(Collectors.joining(","));
        String sql = "INSERT INTO " + table + " (" + columns + ") VALUES (" + placeholders + ")";

        standbyJdbcTemplate.update(sql, cols.values().toArray());
    }

    private  void update(DmlEvent e) {
        Map<String, Object> cols = toMap(e.after());

        Object id = cols.get("id");
        cols.remove("id");

        String setClause = cols.keySet().stream()
                .map(k -> k + "=?")
                .collect(Collectors.joining(","));
        String sql = "UPDATE " + e.table() + " SET " + setClause + " WHERE id=?";
        List<Object> params = new ArrayList<>(cols.values());
        params.add(id);

        standbyJdbcTemplate.update(sql, params.toArray());
    }

    private void delete(DmlEvent e) {
        Map<String, Object> before = toMap(e.before());

        Object id = before.get("id");
        String sql = "DELETE FROM " + e.table() + " WHERE id=?";

        standbyJdbcTemplate.update(sql, id);
    }

    private Map<String, Object> toMap(JsonNode node) {
        Map<String, Object> map = new LinkedHashMap<>();
        node.fieldNames().forEachRemaining(f -> {
            JsonNode v = node.get(f);
            Object value;
            if (v == null || v.isNull()) {
                value = null;
            } else if (isEpochMillisField(f) && v.isNumber()) {
                // Debezium io.debezium.time.Timestamp -> epoch millis
                value = new Timestamp(v.asLong()); // MySQL DATETIME/TIMESTAMP 컬럼에 OK
            } else if (v.isNumber()) {
                value = v.numberValue();
            } else {
                value = v.asText();
            }
            map.put(f, value);
        });
        return map;
    }

    private boolean isEpochMillisField(String fieldName) {
        // 필요한 시간 컬럼을 여기에 추가해서 관리
        return "created_at".equals(fieldName) || "updated_at".equals(fieldName);
    }
}
