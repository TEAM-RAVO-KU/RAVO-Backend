package ravo.ravobackend.global.util;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Shell 명령어 실행 요청을 위한 설정 클래스
 */
@Data
@Builder
public class CommandRequest {
    
    /**
     * 실행할 명령어와 인자들
     */
    private List<String> command;
    
    /**
     * 작업 디렉토리
     */
    private File workingDirectory;
    
    /**
     * 환경 변수
     */
    private Map<String, String> environmentVariables;
    
    /**
     * 표준 입력을 리다이렉션할 파일 (복구 시 덤프 파일)
     */
    private File inputFile;
    
    /**
     * 표준 출력을 리다이렉션할 파일 (백업 시 덤프 파일)
     */
    private File outputFile;
    
    /**
     * 에러 출력을 리다이렉션할 파일
     */
    private File errorFile;
    
    /**
     * 타임아웃 시간 (분)
     */
    private Integer timeoutMinutes;

    /**
     * 에러 출력을 캡처할지 여부
     */
    @Builder.Default
    private boolean captureError = true;
    
    /**
     * 실행 실패 시 예외를 던질지 여부
     */
    @Builder.Default
    private boolean throwOnError = true;
}
