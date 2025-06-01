package ravo.ravobackend.coldStandbyRecovery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ravo.ravobackend.coldStandbyRecovery.controller.request.DumpFileDto;
import ravo.ravobackend.coldStandbyRecovery.controller.request.RecoveryRequest;
import ravo.ravobackend.coldStandbyRecovery.service.ColdStandbyRecoveryService;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recovery")
public class ColdStandbyApiController {

    private final ColdStandbyRecoveryService recoveryService;

    @PostMapping("/api/recovery")
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
