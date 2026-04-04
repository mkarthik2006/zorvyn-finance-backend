package com.zorvyn.finance.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zorvyn.finance.config.SecurityConfig;
import com.zorvyn.finance.dto.record.*;
import com.zorvyn.finance.entity.RecordType;
import com.zorvyn.finance.security.*;
import com.zorvyn.finance.service.FinancialRecordService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinancialRecordController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtAuthenticationEntryPoint.class, JwtAccessDeniedHandler.class})
class FinancialRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FinancialRecordService recordService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRecord_WithValidInput_ShouldReturn201() throws Exception {
        FinancialRecordRequest request = FinancialRecordRequest.builder()
                .amount(new BigDecimal("5000.00"))
                .type(RecordType.INCOME)
                .category("Salary")
                .date(LocalDate.of(2026, 3, 15))
                .notes("Monthly salary")
                .build();

        FinancialRecordResponse response = FinancialRecordResponse.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .type(RecordType.INCOME)
                .category("Salary")
                .date(LocalDate.of(2026, 3, 15))
                .notes("Monthly salary")
                .createdById(1L)
                .createdByName("Admin")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(recordService.createRecord(any(FinancialRecordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.category").value("Salary"));
    }

    @Test
    @WithMockUser(roles = "VIEWER")
    void createRecord_WithViewerRole_ShouldReturn403() throws Exception {
        FinancialRecordRequest request = FinancialRecordRequest.builder()
                .amount(new BigDecimal("5000.00"))
                .type(RecordType.INCOME)
                .category("Salary")
                .date(LocalDate.of(2026, 3, 15))
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRecord_WithNegativeAmount_ShouldReturn400() throws Exception {
        FinancialRecordRequest request = FinancialRecordRequest.builder()
                .amount(new BigDecimal("-100.00"))
                .type(RecordType.EXPENSE)
                .category("Food")
                .date(LocalDate.of(2026, 3, 15))
                .build();

        mockMvc.perform(post("/api/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser(roles = "ANALYST")
    void getRecordById_WithAnalystRole_ShouldReturn200() throws Exception {
        FinancialRecordResponse response = FinancialRecordResponse.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .type(RecordType.INCOME)
                .category("Salary")
                .date(LocalDate.of(2026, 3, 15))
                .createdById(1L)
                .createdByName("Admin")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(recordService.getRecordById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/records/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getRecords_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/records"))
                .andExpect(status().isUnauthorized());
    }
}