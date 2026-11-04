package com.rootl.flux.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.IngestEventRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/events deve ingerir evento e retornar 201 Created")
    void shouldIngestEventSuccessfully() throws Exception {
        IngestEventRequest request = new IngestEventRequest(
                "pedido.criado",
                "servico-checkout",
                "{\"pedidoId\":\"ped-999\",\"valor\":299.90}",
                "idem-unico-1",
                "corr-abc-1",
                null
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.type", is("pedido.criado")))
                .andExpect(jsonPath("$.source", is("servico-checkout")))
                .andExpect(jsonPath("$.idempotencyKey", is("idem-unico-1")))
                .andExpect(jsonPath("$.status", is("RECEIVED")));
    }

    @Test
    @DisplayName("POST /api/v1/events com mesma chave de idempotência deve retornar 409 Conflict")
    void shouldReturnConflictOnDuplicateEvent() throws Exception {
        IngestEventRequest request = new IngestEventRequest(
                "pedido.criado",
                "servico-checkout",
                "{\"pedidoId\":\"ped-1000\"}",
                "idem-duplicado-teste",
                "corr-teste",
                null
        );

        // Primeira chamada é bem sucedida
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segunda chamada falha com 409 Conflito
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Evento Duplicado Detectado")));
    }

    @Test
    @DisplayName("POST /api/v1/events com payload inválido deve retornar 400 Bad Request")
    void shouldReturnBadRequestOnInvalidPayload() throws Exception {
        IngestEventRequest invalidRequest = new IngestEventRequest(
                "",
                "",
                "",
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Falha de Validação nos Parâmetros")));
    }
}
