package ravo.ravobackend.global.constants;

public final class BackupTarget {

    private BackupTarget() {
        // 인스턴스 생성 방지
    }

    /**
     * 주(Primary) 데이터베이스
     */
    public static final String ACTIVE = "active";

    /**
     * 백업용 데이터베이스
     */
    public static final String STANDBY = "standby";
}