package ravo.ravobackend.global.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Shell 명령어 실행을 위한 유틸리티 클래스
 * mysqldump, mysql, mysqlbinlog 등 다양한 명령어 실행을 지원
 */
@Slf4j
@Component
public class ShellCommandExecutor {

    private static final int DEFAULT_TIMEOUT_MINUTES = 30;

    /**
     * 기본 설정으로 명령어 실행
     */
    public CommandResult execute(List<String> command) throws Exception {
        return execute(CommandRequest.builder()
                .command(command)
                .build());
    }

    /**
     * 출력 파일 리다이렉션과 함께 명령어 실행 (백업용)
     */
    public CommandResult executeWithOutputFile(List<String> command, Path outputFile) throws Exception {
        return execute(CommandRequest.builder()
                .command(command)
                .outputFile(outputFile.toFile())
                .build());
    }

    /**
     * 입력 파일 리다이렉션과 함께 명령어 실행 (복구용)
     */
    public CommandResult executeWithInputFile(List<String> command, Path inputFile) throws Exception {
        return execute(CommandRequest.builder()
                .command(command)
                .inputFile(inputFile.toFile())
                .build());
    }

    /**
     * 상세 설정이 가능한 명령어 실행
     */
    public CommandResult execute(CommandRequest request) throws Exception {
        validateRequest(request);

        ProcessBuilder pb = new ProcessBuilder(request.getCommand());
        
        // 작업 디렉토리 설정
        if (request.getWorkingDirectory() != null) {
            pb.directory(request.getWorkingDirectory());
        }

        // 환경 변수 설정
        if (request.getEnvironmentVariables() != null) {
            pb.environment().putAll(request.getEnvironmentVariables());
        }

        // 입출력 리다이렉션 설정
        setupRedirection(pb, request);

        log.info("Executing command: {}", String.join(" ", request.getCommand()));
        
        Process process = pb.start();

        // 출력 캡처 (표준 출력이 리다이렉션되지 않은 경우)
        String output = null;
        String errorOutput = null;

        if (request.getOutputFile() == null) {
            output = captureOutput(process.getInputStream());
        }
        
        if (request.isCaptureError()) {
            errorOutput = captureOutput(process.getErrorStream());
        }

        // 타임아웃 처리
        boolean finished = process.waitFor(
            request.getTimeoutMinutes() != null ? request.getTimeoutMinutes() : DEFAULT_TIMEOUT_MINUTES, 
            TimeUnit.MINUTES
        );

        if (!finished) {
            process.destroyForcibly();
            throw new CommandExecutionException("Command execution timed out: " + String.join(" ", request.getCommand()));
        }

        int exitCode = process.exitValue();
        
        CommandResult result = CommandResult.builder()
                .exitCode(exitCode)
                .output(output)
                .errorOutput(errorOutput)
                .command(request.getCommand())
                .build();

        if (exitCode != 0 && request.isThrowOnError()) {
            throw new CommandExecutionException("Command execution failed with exit code " + exitCode + 
                    ". Command: " + String.join(" ", request.getCommand()) + 
                    (errorOutput != null ? ". Error: " + errorOutput : ""));
        }

        log.info("Command completed with exit code: {}", exitCode);
        return result;
    }

    private void validateRequest(CommandRequest request) {
        if (request.getCommand() == null || request.getCommand().isEmpty()) {
            throw new IllegalArgumentException("Command cannot be null or empty");
        }
    }

    private void setupRedirection(ProcessBuilder pb, CommandRequest request) {
        // 입력 리다이렉션
        if (request.getInputFile() != null) {
            pb.redirectInput(request.getInputFile());
        }

        // 출력 리다이렉션
        if (request.getOutputFile() != null) {
            pb.redirectOutput(request.getOutputFile());
        }

        // 에러 출력 리다이렉션
        if (request.getErrorFile() != null) {
            pb.redirectError(request.getErrorFile());
        } else if (!request.isCaptureError()) {
            pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        }
    }

    private String captureOutput(java.io.InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }
        return output.toString();
    }
}
