package ravo.ravobackend.liveSync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@Slf4j
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

        // Debezium MySQL의 경우 GTID는 source.gtid 로 들어온다 (없을 수도 있으니 널 허용)
        String gtid = source.path("gtid").asText(null);
        Set<String> uuidsInGtid = extractUuidsFromGtid(gtid);


        JsonNode before = payload.path("before");
        JsonNode after = payload.path("after");

        // 파싱 결과 로그
        log.info("[LIVE-SYNC][PARSE] op={}, db={}, table={}, gtid={}, uuids={}", op, db, table, gtid, uuidsInGtid);

        return new DmlEvent(op, db, table, before, after, gtid, uuidsInGtid);
    }

    public DdlEvent parseDdl(String json) throws Exception {
        JsonNode root = om.readTree(json);
        JsonNode payload = root.path("payload");
        String databaseName = payload.path("databaseName").asText();
        String ddl = payload.path("ddl").asText(null);
        return new DdlEvent(databaseName, ddl);
    }

    private Set<String> extractUuidsFromGtid(String gtid) {
        Set<String> set = new LinkedHashSet<>();
        if (gtid == null || gtid.isBlank()) return set;
        String[] parts = gtid.split(",");
        for (String p : parts) {
            String s = p.trim();
            int idx = s.indexOf(':');
            if (idx > 0) {
                set.add(s.substring(0, idx));
            } else {
                // ':' 가 없을 수는 거의 없지만, 방어적으로 전체를 넣지 않고 스킵
            }
        }
        return set;
    }
}
