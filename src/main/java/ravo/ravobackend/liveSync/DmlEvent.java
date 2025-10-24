package ravo.ravobackend.liveSync;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Set;

public record DmlEvent(
        String op, String db, String table, JsonNode before, JsonNode after, String gtid, Set<String> gtidUuids
) {
}
