package ravo.ravobackend.liveSync;

public record DdlEvent(
        String database, String ddl
) {
}
