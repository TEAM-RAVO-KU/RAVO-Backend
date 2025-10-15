package ravo.ravobackend.legacy.hotStandbyRecovery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class DebeziumToSqlConverter {

    private static final String TABLE_NAME = "integrity_data";      // 일단 "integrity_data" table 에 한해서 CDC 적용

    public String convertDebeziumToSQL(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode payload = root.get("payload");
        String op = payload.get("op").asText();

        switch (op) {
            case "c": // INSERT
                return buildInsertSQL(payload.get("after"));
            case "u": // UPDATE
                return buildUpdateSQL(payload.get("before"), payload.get("after"));
            case "d": // DELETE
                return buildDeleteSQL(payload.get("before"));
            default:
                throw new IllegalArgumentException("Unsupported operation type: " + op);
        }
    }
    private String formatMillisToDateTime(long millis) {
        LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
        return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String buildInsertSQL(JsonNode after) {
        int id = after.get("id").asInt();
        String data = after.get("data").isNull() ? null : after.get("data").asText();
        long checkedAt = after.get("checked_at").asLong();
        String checkedAtStr = formatMillisToDateTime(checkedAt);

        return String.format(
                "INSERT INTO %s (id, data, checked_at) VALUES (%d, %s, '%s') "
                + "ON DUPLICATE KEY UPDATE data=VALUES(data), checked_at=VALUES(checked_at);",
                TABLE_NAME,
                id,
                data == null ? "NULL" : "'" + data.replace("'", "''") + "'",
                checkedAtStr
        );
    }

    private String buildUpdateSQL(JsonNode before, JsonNode after) {
        int id = before.get("id").asInt();
        String data = after.get("data").isNull() ? null : after.get("data").asText();
        long checkedAt = after.get("checked_at").asLong();
        String checkedAtStr = formatMillisToDateTime(checkedAt);

        return String.format(
                "UPDATE %s SET data=%s, checked_at='%s' WHERE id=%d;",
                TABLE_NAME,
                data == null ? "NULL" : "'" + data.replace("'", "''") + "'",
                checkedAtStr,
                id
        );
    }


    private String buildDeleteSQL(JsonNode before) {
        int id = before.get("id").asInt();
        return String.format(
                "DELETE FROM %s WHERE id=%d;",
                TABLE_NAME,
                id
        );
    }

}

