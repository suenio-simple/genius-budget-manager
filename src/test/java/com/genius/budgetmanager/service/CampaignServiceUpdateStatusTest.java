package com.genius.budgetmanager.service;

import com.genius.budgetmanager.model.Campaign;
import com.genius.budgetmanager.model.CampaignStatus;
import com.genius.budgetmanager.repository.CampaignRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceUpdateStatusTest {

    @Mock
    private CampaignRepository repository;

    @InjectMocks
    private CampaignService campaignService;

    private Campaign campaign;

    @BeforeEach
    void setUp() {
        campaign = new Campaign(1L, "Campana de prueba", "SuenoSimple", "email", CampaignStatus.DRAFT,
                10000.0, 0.0, "ARS", "2026-01-01", "2026-12-31");
    }

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @CsvSource({
            "active, ACTIVE",
            "ACTIVE, ACTIVE",
            "Active, ACTIVE",
            "aCtIvE, ACTIVE",
            "paused, PAUSED",
            "PAUSED, PAUSED",
            "Paused, PAUSED",
            "closed, CLOSED",
            "CLOSED, CLOSED",
            "cLoSeD, CLOSED",
            "draft, DRAFT",
            "DRAFT, DRAFT",
            "DrAfT, DRAFT",
            "'  active  ', ACTIVE"
    })
    void updateStatus_validStatusInAnyCase_updatesCampaign(String input, CampaignStatus expected) {
        when(repository.findById(1L)).thenReturn(Optional.of(campaign));

        Campaign updated = campaignService.updateStatus(1L, input);

        assertEquals(expected, updated.getStatus());
        assertEquals(expected, campaign.getStatus());
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
            "1",
            "null",
            "\"active\""
    })
    void updateStatus_invalidStatus_throwsIllegalArgumentAndKeepsStatus(String input) {
        when(repository.findById(1L)).thenReturn(Optional.of(campaign));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> campaignService.updateStatus(1L, input));

        assertTrue(ex.getMessage().contains("es inválido"));
        assertEquals(CampaignStatus.DRAFT, campaign.getStatus());
    }

    @ParameterizedTest
    @NullSource
    void updateStatus_nullStatus_throwsIllegalArgumentAndKeepsStatus(String input) {
        when(repository.findById(1L)).thenReturn(Optional.of(campaign));

        assertThrows(IllegalArgumentException.class, () -> campaignService.updateStatus(1L, input));
        assertEquals(CampaignStatus.DRAFT, campaign.getStatus());
    }

    @Test
    void updateStatus_nonExistingCampaign_throwsNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> campaignService.updateStatus(999L, "active"));

        assertEquals("Campaign not found: 999", ex.getMessage());
    }
}
