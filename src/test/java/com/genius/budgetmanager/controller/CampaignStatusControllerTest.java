package com.genius.budgetmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.genius.budgetmanager.model.Campaign;
import com.genius.budgetmanager.model.CampaignStatus;
import com.genius.budgetmanager.repository.CampaignRepository;
import com.genius.budgetmanager.service.CampaignService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Tests unitarios del endpoint PUT /api/campaigns/{id}/status.
@WebMvcTest(CampaignController.class)
@Import(CampaignService.class)
class CampaignStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CampaignRepository repository;

    private Campaign campaign;

    @BeforeEach
    void setUp() {
        campaign = new Campaign(1L, "Campana de prueba", "SuenoSimple", "email", CampaignStatus.PAUSED,
                10000.0, 0.0, "ARS", "2026-01-01", "2026-12-31");
        when(repository.findById(1L)).thenReturn(Optional.of(campaign));
        when(repository.findById(999L)).thenReturn(Optional.empty());
    }

    private String statusBody(String status) throws Exception {
        return objectMapper.writeValueAsString(Map.of("status", status));
    }

    @ParameterizedTest(name = "\"{0}\" es rechazado")
    @ValueSource(strings = {
            "",
            "   ",
            "foo",
            "activo",
            "activ",
            "actives",
            "act ive",
            "active_",
            "ACTIVE,PAUSED",
            "0",
            "null"
    })
    void updateStatus_invalidStatus_returns400(String status) throws Exception {
        mockMvc.perform(put("/api/campaigns/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody(status)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        assertEquals(CampaignStatus.PAUSED, campaign.getStatus());
    }

    @ParameterizedTest(name = "body {0} es rechazado")
    @ValueSource(strings = {
            // sin campos / campos de menos
            "{}",
            "{\"status\": null}",
            "",
            "null",
            // campos extras o incorrectos
            "{\"status\": \"active\", \"extra\": 1}",
            "{\"status\": \"active\", \"name\": \"otro nombre\"}",
            "{\"estado\": \"active\"}",
            // tipos incorrectos
            "{\"status\": 1}",
            "{\"status\": true}",
            "{\"status\": [\"active\"]}",
            "{\"status\": {\"value\": \"active\"}}",
            "[]",
            "\"active\"",
            // JSON mal formado
            "not json",
            "{\"status\": \"active\""
    })
    void updateStatus_invalidBody_returns400(String body) throws Exception {
        mockMvc.perform(put("/api/campaigns/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        assertEquals(CampaignStatus.PAUSED, campaign.getStatus());
    }

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "active, active",
            "ACTIVE, active",
            "Active, active",
            "aCtIvE, active",
            "paused, paused",
            "PAUSED, paused",
            "PaUsEd, paused",
            "closed, closed",
            "CLOSED, closed",
            "Closed, closed",
            "draft, draft",
            "DRAFT, draft",
            "dRaFt, draft",
            "'  Active  ', active"
    })
    void updateStatus_validStatusInAnyCase_returns200(String status, String expected) throws Exception {
        mockMvc.perform(put("/api/campaigns/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody(status)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(expected));
    }

    @Test
    void updateStatus_nonExistingCampaign_returns404() throws Exception {
        mockMvc.perform(put("/api/campaigns/999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody("active")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Campaign not found: 999"));
    }

    @Test
    void updateStatus_validRequest_returns200WithUpdatedCampaign() throws Exception {
        mockMvc.perform(put("/api/campaigns/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusBody("closed")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Campana de prueba"))
                .andExpect(jsonPath("$.client").value("SuenoSimple"))
                .andExpect(jsonPath("$.budget").value(10000.0))
                .andExpect(jsonPath("$.status").value("closed"));

        assertEquals(CampaignStatus.CLOSED, campaign.getStatus());
    }
}
