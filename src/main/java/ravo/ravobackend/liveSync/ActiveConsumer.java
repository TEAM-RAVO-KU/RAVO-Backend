package ravo.ravobackend.liveSync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;
import ravo.ravobackend.hotStandbyRecovery.DebeziumToSqlConverter;
import ravo.ravobackend.hotStandbyRecovery.SqlQueueService;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveConsumer {

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    private final SqlQueueService sqlQueueService;
    private final DebeziumToSqlConverter debeziumToSqlConverter;

    @KafkaListener(
            id = "active-users-consumer",
            topics = "ravo_db.ravo_db.users",
            groupId = "ravo-live-sync-group-test",
            containerFactory = "kafkaListenerContainerFactory",
            properties = {
                    "auto.offset.reset=earliest",
                    "isolation.level=read_committed",
                    "session.timeout.ms=45000"
            }
    )
    public void listen(
            ConsumerRecord<String, String> record
    ) throws Exception {

        log.info("[ASSIGN TEST] topic={}, partition={}, offset={}, key={}, value={}",
                record.topic(), record.partition(), record.offset(), record.key(), record.value());

//        String sql = debeziumToSqlConverter.convertDebeziumToSQL(record.value());
//        log.info("build sql: " + sql);
//
//        sqlQueueService.enqueue(sql);
    }
}
