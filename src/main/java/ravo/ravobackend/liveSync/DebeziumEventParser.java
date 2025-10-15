package ravo.ravobackend.liveSync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class DebeziumEventParser {

    private final ObjectMapper om = new ObjectMapper();

    public DmlEvent parseDml(String json) throws Exception {
        JsonNode root = om.readTree(json);
        JsonNode payload = root.path("payload");
        if (payload.isMissingNode()) return null;

        String op = payload.path("op").asText();    // c/u/d/t
        if ("t".equals(op)) return null;    // 트랜잭션 경계는 스킵

        JsonNode source = payload.path("source");
        String db = source.path("db").asText();
        String table = source.path("table").asText();

        JsonNode before = payload.path("before");
        JsonNode after = payload.path("after");

        return new DmlEvent(op, db, table, before, after);
    }

    public DdlEvent parseDdl(String json) throws Exception {
        JsonNode root = om.readTree(json);
        JsonNode payload = root.path("payload");
        String databaseName = payload.path("databaseName").asText();
        String ddl = payload.path("ddl").asText(null);
        return new DdlEvent(databaseName, ddl);
    }
}
