package ravo.ravobackend.hotStandbyPoc;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;


@SpringBootTest
class MyKafkaTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Test
    public void sendMessage() {
        kafkaTemplate.send("spring-test-topic", "test!!");
    }

}