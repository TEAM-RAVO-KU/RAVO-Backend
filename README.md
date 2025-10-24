# RAVO-Backend
RAVO의 핵심 백엔드 서버입니다.

  * **Live Sync**

      * `Kafka`의 CDC 토픽을 실시간으로 Subscribe합니다.
      * `Debezium`이 감지한 Active DB의 Insert, Update, Delete등 데이터 변경 쿼리를 파싱하여 Standby DB에 적용함으로써 Live Sync를 유지합니다.

  * **Auto-Recover**

      * 장애가 복구된 Active DB의 상태를 헬스체킹을 통해 감지합니다.
      * Failover가 일어난 시점부터 Active DB가 복구된 시점까지 Standby DB에 쌓인 모든 변경 내역을 `GTID`를 기반으로 추적합니다.
      * 추적된 변경분을 Active DB에 역방향으로 적용하여 데이터 정합성을 보장합니다.
      * 정합성 보장 후, `RAVO-AGENT`의 `/recover` API를 호출하여 서비스 트래픽을 다시 Active DB로 전환하도록 명령합니다.

  * **Dispersion Backup**

      * `@Scheduled`를 이용해 1일 1회 등 주기적으로 Active DB의 전체 데이터를 `mysqldump` 합니다.
      * 덤프 파일은 Active DB가 위치한 클러스터와 물리적/논리적으로 분리된 On-Premise 클러스터 내의 Persistent Volume에 저장하여 IDC 전체 재해에 대비합니다.

  * **DB Health Checking**

      * Active DB와 Standby DB의 Health을 주기적으로 확인하고 `RAVO-MANAGER` 대시보드로 상태를 리포트합니다.
