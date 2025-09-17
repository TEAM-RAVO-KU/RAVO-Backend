package ravo.ravobackend.global.util;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Shell 명령어 실행 결과
 */
@Data
@Builder
public class CommandResult {
    
    /**
     * 명령어 종료 코드
     */
    private int exitCode;
    
    /**
     * 표준 출력 내용
     */
    private String output;
    
    /**
     * 에러 출력 내용
     */
    private String errorOutput;
    
    /**
     * 실행된 명령어
     */
    private List<String> command;
    
    /**
     * 명령어가 성공적으로 실행되었는지 확인
     */
    public boolean isSuccess() {
        return exitCode == 0;
    }
    
    /**
     * 실행된 명령어를 문자열로 반환
     */
    public String getCommandString() {
        return String.join(" ", command);
    }
}
