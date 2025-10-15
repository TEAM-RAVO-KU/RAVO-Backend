package ravo.ravobackend.liveSync;

import com.fasterxml.jackson.databind.JsonNode;

public record DmlEvent(
        String op, String db, String table, JsonNode before, JsonNode after
) {
}
