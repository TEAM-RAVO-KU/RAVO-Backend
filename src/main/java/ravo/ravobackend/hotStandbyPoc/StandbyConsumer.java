package ravo.ravobackend.hotStandbyPoc;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static ravo.ravobackend.hotStandbyPoc.DebeziumToSqlConverter.convertDebeziumToSQL;

@Slf4j
@Component
public class StandbyConsumer {

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    private List<String> queue = new LinkedList<>();

    @Autowired
    @Qualifier("activeJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    @KafkaListener(
            id = "integrity-data-standby",
            topics = "ravo_db.ravo_db.integrity_data",
            groupId = "ravo-standby-test",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(
            ConsumerRecord<String, String> record,
            Consumer<?, ?> consumer
    ) throws Exception {
        String sql = convertDebeziumToSQL(record.value());
        log.info("build sql: "+ sql);
        queue.add(sql);

        System.out.println("queue 내부 정보");
        for(String s: queue) {
            System.out.println(s);
        }

        jdbcTemplate.execute(sql);
    }
}

