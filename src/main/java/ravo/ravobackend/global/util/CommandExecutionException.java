package ravo.ravobackend.global.util;

/**
 * Shell 명령어 실행 실패 시 발생하는 예외
 */
public class CommandExecutionException extends RuntimeException {
    
    public CommandExecutionException(String message) {
        super(message);
    }
    
    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
