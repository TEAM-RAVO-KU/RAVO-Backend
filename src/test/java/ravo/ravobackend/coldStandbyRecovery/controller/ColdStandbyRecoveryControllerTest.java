package ravo.ravobackend.coldStandbyRecovery.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ravo.ravobackend.coldStandbyRecovery.controller.request.RecoveryRequest;
import ravo.ravobackend.coldStandbyRecovery.service.ColdStandbyRecoveryService;

import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ColdStandbyApiController.class)
class ColdStandbyRecoveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ColdStandbyRecoveryService recoveryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/recovery - 성공")
    void recover_success() throws Exception {
        String fileName = "active_db_cold_yyyyhhmmss";
        RecoveryRequest request = new RecoveryRequest(fileName);

        mockMvc.perform(post("/api/recovery").contentType("application/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("recovery success : " + fileName));

        then(recoveryService).should(times(1)).recover(fileName);
    }

    @Test
    @DisplayName("POST /api/recovery - 예외 발생 시 500 반환")
    void recover_fail() throws Exception {
        String fileName = "bad request";
        RecoveryRequest request = new RecoveryRequest(fileName);

        willThrow(new RuntimeException("recover failed"))
                .given(recoveryService).recover(fileName);

        mockMvc.perform(post("/api/recovery")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        then(recoveryService).should(times(1)).recover(fileName);
    }
}