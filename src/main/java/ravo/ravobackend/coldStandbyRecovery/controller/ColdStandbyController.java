package ravo.ravobackend.coldStandbyRecovery.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ravo.ravobackend.coldStandbyRecovery.controller.request.DumpFileDto;
import ravo.ravobackend.coldStandbyRecovery.service.ColdStandbyRecoveryService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/recovery")
public class ColdStandbyController {

    private final ColdStandbyRecoveryService recoveryService;

    @GetMapping
    public String showRecoveryPage(Model model) throws IOException {
        List<DumpFileDto> dumps;
        dumps = recoveryService.listDumpFiles();
        log.info("showRecovery page");
        log.info("dumps: {}", dumps);
        model.addAttribute("dumps", dumps);
        return "recovery/index";
    }

    @PostMapping
    public String recoverByForm(@RequestParam("filename") String filename, Model model) throws Exception {
        if (filename == null || filename.isBlank()) {
            model.addAttribute("errorMessage", "선택된 파일명이 없습니다.");
            return "recovery/index";
        }
        recoveryService.recover(filename.trim());
        model.addAttribute("successMessage", "복구 작업이 완료되었습니다: " + filename);

        List<DumpFileDto> dumps = recoveryService.listDumpFiles();
        model.addAttribute("dumps", dumps);
        return "recovery/index";
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleError(Exception ex) {
        return ResponseEntity
                .status(500)
                .body("Failed to recovery: " + ex.getMessage());
    }

}
