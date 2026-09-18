package com.genius.budgetmanager.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Tests e2e del endpoint PUT /api/campaigns/{id}/status
// Modifican el repositorio en memoria
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CampaignStatusE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @SuppressWarnings("rawtypes")
    private ResponseEntity<Map> putStatus(long id, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange("/api/campaigns/" + id + "/status", HttpMethod.PUT,
                new HttpEntity<>(body, headers), Map.class);
    }

    private Object getStatus(long id) {
        ResponseEntity<Map> response = restTemplate.getForEntity("/api/campaigns/" + id, Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody().get("status");
    }

    @ParameterizedTest(name = "body {0} no modifica el status")
    @ValueSource(strings = {
            "{\"status\": \"foo\"}",
            "{\"status\": \"\"}",
            "{\"status\": null}",
            "{}",
            "{\"status\": \"active\", \"extra\": 1}",
            "{\"status\": [\"active\"]}",
            "not json"
    })
    void updateStatus_failedRequest_keepsOriginalStatus(String body) {
        assertEquals("paused", getStatus(4));

        ResponseEntity<Map> response = putStatus(4, body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("paused", getStatus(4));
    }

    @Test
    void updateStatus_thenGetById_reflectsNewStatus() {
        assertEquals("active", getStatus(2));

        ResponseEntity<Map> response = putStatus(2, "{\"status\": \"Paused\"}");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("paused", response.getBody().get("status"));
        assertEquals("paused", getStatus(2));

        ResponseEntity<Map> byId = restTemplate.getForEntity("/api/campaigns/2", Map.class);
        assertEquals("Email Recupero de Carritos", byId.getBody().get("name"));

        putStatus(2, "{\"status\": \"CLOSED\"}");
        assertEquals("closed", getStatus(2));
    }
}
