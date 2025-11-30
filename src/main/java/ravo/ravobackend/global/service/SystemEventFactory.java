package ravo.ravobackend.global.service;

import ravo.ravobackend.global.domain.EventType;
import ravo.ravobackend.global.domain.Severity;
import ravo.ravobackend.global.domain.SystemEvent;

import java.time.Instant;

public class SystemEventFactory {

    private static String now() {
        return Instant.now().toString();
    }

    /** Active DB 장애 감지 */
    public static SystemEvent activeDbFailure(String details) {
        return SystemEvent.builder()
                .eventType(EventType.CONNECTION)
                .severity(Severity.ERROR)
                .title("Active DB 장애 발생")
                .description("Active DB 응답이 없거나 상태 점검에 실패했습니다.")
                .timestamp(now())
                .details(details)
                .build();
    }

    /** Active DB 복구 감지 */
    public static SystemEvent activeDbRecover(String details) {
        return SystemEvent.builder()
                .eventType(EventType.CONNECTION)
                .severity(Severity.SUCCESS)
                .title("Active DB 복구 감지")
                .description("Active DB 정상 작동을 감지했습니다.")
                .timestamp(now())
                .details(details)
                .build();
    }

    /** Standby DB로 Failover 성공 */
    public static SystemEvent failoverSuccess(String details) {
        return SystemEvent.builder()
                .eventType(EventType.CONNECTION)
                .severity(Severity.SUCCESS)
                .title("Failover 완료")
                .description("시스템이 자동으로 Standby DB로 전환되었습니다.")
                .timestamp(now())
                .details(details)
                .build();
    }

    /** Standby → Active 재동기화 완료 */
    public static SystemEvent resyncCompleted(String details) {
        return SystemEvent.builder()
                .eventType(EventType.SYNC)
                .severity(Severity.SUCCESS)
                .title("DB 재동기화 완료")
                .description("Standby DB가 Active DB 상태와 성공적으로 동기화되었습니다.")
                .timestamp(now())
                .details(details)
                .build();
    }

    /** 시스템 시작 / 서비스 정상 가동 */
    public static SystemEvent systemStarted(String details) {
        return SystemEvent.builder()
                .eventType(EventType.CONNECTION)
                .severity(Severity.SUCCESS)
                .title("시스템 구동 완료")
                .description("서비스 연결 및 초기화가 정상적으로 완료되었습니다.")
                .timestamp(now())
                .details(details)
                .build();
    }

    /** 복구 프로세스 시작 */
    public static SystemEvent recoveryStarted(String details) {
        return SystemEvent.builder()
                .eventType(EventType.RECOVERY)
                .severity(Severity.INFO)
                .title("데이터 복구 작업 시작")
                .description("백업 또는 Binlog 기반 복구 작업이 시작되었습니다.")
                .timestamp(now())
                .details(details)
                .build();
    }

    /** 복구 실패 */
    public static SystemEvent recoveryFailed(String details) {
        return SystemEvent.builder()
                .eventType(EventType.RECOVERY)
                .severity(Severity.ERROR)
                .title("복구 실패")
                .description("데이터 복구 작업이 실패했습니다. 상세 정보를 확인하세요.")
                .timestamp(now())
                .details(details)
                .build();
    }

    /** 시스템 상태 양호 */
    public static SystemEvent systemHealthy() {
        return SystemEvent.builder()
                .eventType(EventType.CONNECTION)
                .severity(Severity.SUCCESS)
                .title("시스템 정상 상태")
                .description("모든 구성 요소가 정상적으로 동작 중입니다.")
                .timestamp(now())
                .details(null)
                .build();
    }

    /** 백업 완료 */
    public static SystemEvent dumpBackupComplete(String details) {
        return SystemEvent.builder()
                .eventType(EventType.BACKUP)
                .severity(Severity.SUCCESS)
                .title("백업 완료")
                .description("데이터 덤프 백업이 정상적으로 완료되었습니다.")
                .timestamp(now())
                .details(details)
                .build();
    }
}
