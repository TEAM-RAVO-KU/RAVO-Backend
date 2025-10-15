package ravo.ravobackend.liveSync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.GTID;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.service.GtidService;
import ravo.ravobackend.global.constants.TargetDB;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActiveChangeListener {

    private final ChangeDispatcher dispatcher;
    private final GtidService gtidService;

    @KafkaListener(
            id = "${ravo.live-sync.listener-id}",
            topicPattern = "${ravo.live-sync.topic-pattern}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record, Acknowledgment ack) throws Exception {
        try {
            if (record.value() == null) {
                log.warn("[LIVE-SYNC] Skip null record. topic={}, partition={}, offset={}",
                        record.topic(), record.partition(), record.offset());
                ack.acknowledge();  // null 레코드는 반영할 게 없으므로 커밋
                return;
            }

            log.info("[LIVE-SYNC] Received record: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());

            dispatcher.dispatch(record);

            log.info("--------------------");

            GTID currentGtidFromStandby = gtidService.getCurrentGtidFromStandby();
            gtidService.saveGtid(TargetDB.STANDBY, currentGtidFromStandby);

            log.info("==================");

            ack.acknowledge();  // standby DB에 반영 성공한 뒤에만 커밋
        } catch (Exception e) {
            log.error("[LIVE-SYNC] Failed to apply change. topic={}, partition={}, offset={}, err={}",
                    record.topic(), record.partition(), record.offset(), e.getMessage(), e);
            throw e;    // 커밋 X & exception throw -> 재시도
        }
    }
}
