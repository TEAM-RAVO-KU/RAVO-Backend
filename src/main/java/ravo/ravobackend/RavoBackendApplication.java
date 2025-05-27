package ravo.ravobackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class RavoBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RavoBackendApplication.class, args);
    }

}
