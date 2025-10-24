package ravo.ravobackend.liveSync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChangeDispatcher {

    private final DebeziumEventParser parser;
    private final DmlApplier dmlApplier;
    private final DdlApplier ddlApplier;
    private final ActiveUuidService activeUuidService;

    public boolean dispatch(ConsumerRecord<String, String> record) throws Exception {
        final String topic = record.topic();
        final String value = record.value();

        if (isMetaTopic(topic)) {   // 메타테이블 필터링
            // 로그만 남기고 스킵
            System.out.printf("[LIVE-SYNC] Skip meta topic: %s%n", topic);
            return false;
        }

        if ("ravo_db".equals(topic)) {      // 스키마 체인지(DDL) 토픽
            DdlEvent ddl = parser.parseDdl(value);
            ddlApplier.apply(ddl);
            return true;
        }

        if (topic.startsWith("ravo_db.ravo_db.")) {     // 테이블 DML
            DmlEvent dml = parser.parseDml(value);
            if (dml == null) return false;

            // 1) 메시지의 GTID에 ACTIVE uuid가 들어있는가?
            String activeUuid = activeUuidService.getActiveUuid();
            boolean fromActive = dml.gtidUuids() != null && dml.gtidUuids().contains(activeUuid);

            if (!fromActive) {
                log.info("[LIVE-SYNC] Skip DML (uuid mismatch). topic={}, table={}, gtid={}, uuids={}, activeUuid={}",
                        topic, dml.table(), dml.gtid(), dml.gtidUuids(), activeUuid);
                return false;
            }

            // 2) ACTIVE에서 발생한 DML만 standby에 반영
            dmlApplier.apply(dml);
            log.info("[LIVE-SYNC] DML applied. table={}, op={}, gtid={}", dml.table(), dml.op(), dml.gtid());
            return true;
        }

        if ("ravo_db.heartbeat".equals(topic)) {    // 하트비트 -> 일단 무시
            return false;
        }

        // 그 외에는?? -> 일단 무시
        return false;
    }

    private boolean isMetaTopic(String topic) {
        if (!topic.startsWith("ravo_db.ravo_db.")) return false;
        String[] parts = topic.split("\\.");
        if (parts.length < 3) return false;
        String tableName = parts[2];
        return tableName.startsWith("BATCH_")
                || tableName.startsWith("flyway_schema_history")
                || tableName.startsWith("QUARTZ_");
    }
}
