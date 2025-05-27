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
public class MyKafkaListener {

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    /**
     * 컨텍스트 기동
     * → Kafka 리스너 컨테이너 생성·시작
     * → 첫 메시지 수신 시 listenOnce() 호출
     * → 메시지 로그 출력
     * → 컨테이너 바로 중지
     */
    @KafkaListener(
            id = "integrity-data-once",
            topics = "ravo_db.ravo_db.integrity_data",
            groupId = "ravo-test",
            containerFactory = "kafkaListenerContainerFactory"      // 기본 펙토리
    )
    public void listenOnce(
            ConsumerRecord<String, String> record,
            Consumer<?, ?> consumer
    ) {
        log.info("groupId = {}, topic = {}, msg = {}",
                consumer.groupMetadata().groupId(),
                record.topic(),
                record.value());

        // 오프셋 커밋(자동 커밋이 아니면 필요)
        // acknowledge.acknowledge();

        // 해당 리스너 컨테이너만 중지
        registry.getListenerContainer("integrity-data-once").stop();
    }
}
