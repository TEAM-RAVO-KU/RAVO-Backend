package ravo.ravobackend.recovery.manual.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ravo.ravobackend.recovery.manual.controller.request.DumpFileDto;
import ravo.ravobackend.recovery.manual.controller.request.RecoveryRequest;
import ravo.ravobackend.recovery.manual.service.ManualRecoveryService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recovery")
public class ManualRecoveryApiController {

    private final ManualRecoveryService recoveryService;

    @GetMapping
    public ResponseEntity<List<DumpFileDto>> listDumps() throws IOException {
        List<DumpFileDto> dumps = recoveryService.listDumpFiles();
        return ResponseEntity.ok(dumps);
    }

    @PostMapping
    public ResponseEntity<String> recover(@RequestBody RecoveryRequest request) throws Exception {
        recoveryService.recover(request.getFileName());
        return ResponseEntity.ok("recovery success : " + request.getFileName());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleError(Exception ex) {
        return ResponseEntity
                .status(500)
                .body("Failed to recovery: " + ex.getMessage());
    }
}
