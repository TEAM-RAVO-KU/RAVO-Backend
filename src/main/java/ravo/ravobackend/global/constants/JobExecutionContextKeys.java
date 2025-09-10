package ravo.ravobackend.global.constants;

public final class JobExecutionContextKeys {

    private JobExecutionContextKeys() {
    }

    /**
     * 백업 디렉토리 경로를 저장할 때 사용하는 키
     */
    public static final String BACKUP_OUT_FILE = "backupOutFile";
    /**
     * 타겟 데이터베이스 속성을 저장할 때 사용하는 키
     */
    public static final String TARGET_DATABASE_PROPERTIES = "targetDatabaseProperties";
}

