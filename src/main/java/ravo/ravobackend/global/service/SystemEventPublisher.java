package ravo.ravobackend.global.service;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ravo.ravobackend.global.domain.SystemEvent;

@Service
@RequiredArgsConstructor
public class SystemEventPublisher {

    private final KafkaTemplate<String, SystemEvent> kafkaTemplate;

    public void publish(SystemEvent event) {
        kafkaTemplate.send("ravo.system.events", event);
    }

    @PostConstruct
    public void init() {
        publish(SystemEventFactory.systemStarted(null));
    }
}
