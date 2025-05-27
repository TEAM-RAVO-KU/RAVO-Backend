package ravo.ravobackend.hotStandbyPoc;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StandbyConsumer {

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @KafkaListener(
            id = "integrity-data-standby",
            topics = "ravo_db.ravo_db.integrity_data",
            groupId = "ravo-standby-test",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(
            ConsumerRecord<String, String> record,
            Consumer<?, ?> consumer
    ) {
        log.info("groupId = {}, topic = {}, msg = {}",
                consumer.groupMetadata().groupId(),
                record.topic(),
                record.value());
    }
}
