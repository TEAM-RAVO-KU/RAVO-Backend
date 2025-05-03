package ravo.ravobackend.coldstandby.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ravo.ravobackend.coldstandby.service.ColdStandbyService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/coldstandby")
public class ColdStandbyController {

    private final ColdStandbyService coldStandbyService;

    @PostMapping("/recovery")
    public ResponseEntity<String> recover() throws Exception {
        long ts = coldStandbyService.launchRecovery();
        return ResponseEntity
                .ok("Recovery job started at ts=" + ts);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleError(Exception ex) {
        return ResponseEntity
                .status(500)
                .body("Failed to start recovery job: " + ex.getMessage());
    }
}
