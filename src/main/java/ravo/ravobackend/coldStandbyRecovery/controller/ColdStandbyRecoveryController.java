package ravo.ravobackend.coldStandbyRecovery.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ravo.ravobackend.coldStandbyRecovery.controller.request.RecoveryRequest;
import ravo.ravobackend.coldStandbyRecovery.service.ColdStandbyRecoveryService;

@RestController
@RequiredArgsConstructor
public class ColdStandbyRecoveryController {

    private final ColdStandbyRecoveryService recoveryService;

    @PostMapping("/api/recovery")
    public ResponseEntity<String> recover(@RequestBody RecoveryRequest request) throws Exception {
        recoveryService.recover(request.getFileName());
        return ResponseEntity.ok("recovery success : " + request.getFileName());
    }
}
