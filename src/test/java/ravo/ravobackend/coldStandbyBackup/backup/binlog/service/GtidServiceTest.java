package ravo.ravobackend.coldStandbyBackup.backup.binlog.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.GTID;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.domain.GtidHistory;
import ravo.ravobackend.coldStandbyBackup.backup.binlog.repository.GtidRepository;
import ravo.ravobackend.global.constants.TargetDB;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


class GtidServiceTest {

    @Mock
    private GtidRepository gtidRepository;

    @Mock
    private JdbcTemplate standbyJdbcTemplate;

    @InjectMocks
    private GtidService gtidService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("StandbyDB의 현재 GTID 상태를 조회한다")
    void getCurrentGtidSetFromStandby_should_Return_ParsedGtid() {
        //given
        when(standbyJdbcTemplate.queryForObject("SELECT @@GLOBAL.gtid_executed",
                String.class
        )).thenReturn("uuid:1-10");

        //when
        GTID currentGtidFromStandby = gtidService.getCurrentGtidFromStandby();

        //then
        Assertions.assertThat(currentGtidFromStandby.toString()).isEqualTo("uuid:1-10");
    }

    @Test
    @DisplayName("GTID를 전달하고 GtidHistory 객체를 생성 후 저장한다")
    void saveGitd_should_Save_GtidHistory() {
        //given
        GTID gtid = GTID.parse("uuid:1-10");
        GtidHistory gtidHistory = GtidHistory.builder()
                .dbName("testdb")
                .gtidSet(gtid.toString())
                .build();

        when(gtidRepository.save(any(GtidHistory.class))).thenReturn(gtidHistory);

        //when
        GtidHistory saved = gtidService.saveGtid(TargetDB.STANDBY, gtid);

        //then
        Assertions.assertThat(saved.getDbName()).isEqualTo("testdb");
        Assertions.assertThat(saved.getGtidSet()).isEqualTo("uuid:1-10");
    }

    @Test
    @DisplayName("GTID 범위의 차이를 계산한다")
    void calculateGtidRange_should_Return_GtidRange() {
        //given
        GTID current = GTID.parse("uuid:1-10");
        GTID last = GTID.parse("uuid:1-5");
        //when
        Optional<GTID> result = gtidService.calculateGtidRange(current, last);

        //then
        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get().toString()).isEqualTo("uuid:6-10");
    }

    @Test
    @DisplayName("GTID 범위에 차이가 없는 경우 Empty를 반환한다")
    void calculateGtidRange_should_Return_Empty_If_NoDiff() {
        //given
        GTID gtid = GTID.parse("uuid:1-10");

        //when
        Optional<GTID> result = gtidService.calculateGtidRange(gtid, gtid);

        //then
        Assertions.assertThat(result).isEmpty();
    }


}