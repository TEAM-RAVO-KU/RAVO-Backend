package ravo.ravobackend.hotStandbyPoc;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

@Component
public class MyKafkaListener {
    @KafkaListener(topicPartitions = @TopicPartition(
            topic = "test-topic-3",
            partitions = {"0"} // 0번, 1번 파티션만 구독
    ), groupId = "ravo-test")
    public void listen(String message) {
        System.out.println("Received from kafka: " + message);
    }
}
