//package ravo.ravobackend.hotStandbyRecovery;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.Consumer;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class StandbyConsumer {
//
//    @Autowired
//    private KafkaListenerEndpointRegistry registry;
//
//    private final SqlQueueService sqlQueueService;
//    private final DebeziumToSqlConverter debeziumToSqlConverter;
//
//    @KafkaListener(
//            id = "integrity-data-standby",
//            topics = "ravo_db.ravo_db.integrity_data",
//            groupId = "ravo-standby-recovery",
//            containerFactory = "kafkaListenerContainerFactory"
//    )
//    public void listen(
//            ConsumerRecord<String, String> record,
//            Consumer<?, ?> consumer
//    ) throws Exception {
//        String sql = debeziumToSqlConverter.convertDebeziumToSQL(record.value());
//        log.info("build sql: " + sql);
//
//        sqlQueueService.enqueue(sql);
//    }
//}
