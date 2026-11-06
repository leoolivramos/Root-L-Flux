package com.rootl.flux.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rootl.flux.application.port.out.MetricsPort;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.IngestEventRequest;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Testes de Integração de Observabilidade e Métricas")
class ObservabilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private MetricsPort metricsPort;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve expor o health check operacional com indicador customizado do Root L Flux")
    void shouldExposeCustomHealthIndicator() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.flux.status").value("UP"))
                .andExpect(jsonPath("$.components.flux.details.plataforma").value("Root L Flux"))
                .andExpect(jsonPath("$.components.flux.details.statusOperacional").value("Operacional"));
    }

    @Test
    @DisplayName("Deve expor o endpoint de scraping do Prometheus com métricas formatadas")
    void shouldExposePrometheusScrapeEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        assertNotNull(responseContent);
        assertTrue(responseContent.contains("jvm_") || responseContent.contains("process_"),
                "A resposta do Prometheus deve conter métricas da JVM ou do processo");
    }

    @Test
    @DisplayName("Deve injetar cabeçalhos de correlação X-Correlation-Id e X-Trace-Id nas respostas HTTP")
    void shouldInjectCorrelationHeadersInHttpResponses() throws Exception {
        String correlationId = "corr-" + UUID.randomUUID();

        mockMvc.perform(get("/api/v1/dlq")
                        .header("X-Correlation-Id", correlationId))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlation-Id", correlationId))
                .andExpect(header().exists("X-Trace-Id"));
    }

    @Test
    @DisplayName("Deve contabilizar métricas customizadas de negócio no MeterRegistry do Micrometer")
    void shouldRecordCustomBusinessMetrics() throws Exception {
        String key = "chave-obs-" + UUID.randomUUID();
        IngestEventRequest request = new IngestEventRequest(
                "evento.observabilidade",
                "servico-monitor",
                "{\"status\":\"metric-test\"}",
                key,
                "corr-metric-1",
                null
        );

        // 1. Ingestão inicial (Sucesso)
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // 2. Ingestão duplicada (Conflito 409)
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        // Valida contadores registrados no Micrometer
        double ingestedCount = meterRegistry.get("flux.events.ingested")
                .tag("type", "evento.observabilidade")
                .counter()
                .count();

        double duplicateCount = meterRegistry.get("flux.events.duplicates")
                .tag("type", "evento.observabilidade")
                .counter()
                .count();

        assertTrue(ingestedCount >= 1.0, "O contador de eventos ingeridos deve ter sido incrementado");
        assertTrue(duplicateCount >= 1.0, "O contador de duplicados deve ter sido incrementado");
    }
}
