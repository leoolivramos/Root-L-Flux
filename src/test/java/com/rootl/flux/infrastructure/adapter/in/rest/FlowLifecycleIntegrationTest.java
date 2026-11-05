package com.rootl.flux.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.CreateFlowDefinitionRequest;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.EventResponse;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.IngestEventRequest;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.StepDefinitionDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlowLifecycleIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Ciclo completo: cadastrar fluxo, disparar via evento, consultar etapas e reexecutar")
    void shouldExecuteFullFlowLifecycle() throws Exception {
        // 1. Cadastrar Definição de Fluxo
        CreateFlowDefinitionRequest flowRequest = new CreateFlowDefinitionRequest(
                "fluxo-pedidos-e2e",
                "Fluxo Completo de Pedidos",
                "pedido.aprovado",
                List.of(
                        new StepDefinitionDto("validar-estoque", "Validar Estoque", 1, "VALIDACAO", 3),
                        new StepDefinitionDto("emitir-nota", "Emitir Nota Fiscal", 2, "INTEGRACAO", 2)
                )
        );

        mockMvc.perform(post("/api/v1/flows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(flowRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.flowId", is("fluxo-pedidos-e2e")))
                .andExpect(jsonPath("$.steps", hasSize(2)));

        // 2. Ingerir evento disparador
        IngestEventRequest eventRequest = new IngestEventRequest(
                "pedido.aprovado",
                "servico-checkout",
                "{\"pedidoId\":\"ped-e2e-1\",\"total\":450.0}",
                "idem-e2e-pedido-1",
                "corr-e2e-1",
                null
        );

        MvcResult eventResult = mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andReturn();

        EventResponse eventResponse = objectMapper.readValue(
                eventResult.getResponse().getContentAsString(),
                EventResponse.class
        );

        // 3. Disparar execução manualmente para verificação explícita do endpoint /executions/trigger
        MvcResult execResult = mockMvc.perform(post("/api/v1/executions/trigger")
                        .param("flowId", "fluxo-pedidos-e2e")
                        .param("eventId", eventResponse.id()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.flowId", is("fluxo-pedidos-e2e")))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andReturn();

        String executionId = objectMapper.readTree(execResult.getResponse().getContentAsString())
                .get("id").asText();

        // 4. Consultar execução e verificar etapas executadas
        mockMvc.perform(get("/api/v1/executions/" + executionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(executionId)))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.steps", hasSize(2)))
                .andExpect(jsonPath("$.steps[0].stepId", is("validar-estoque")))
                .andExpect(jsonPath("$.steps[0].status", is("COMPLETED")))
                .andExpect(jsonPath("$.steps[1].stepId", is("emitir-nota")))
                .andExpect(jsonPath("$.steps[1].status", is("COMPLETED")));

        // 5. Executar Flow Replay e verificar sucesso
        mockMvc.perform(post("/api/v1/executions/" + executionId + "/replay"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(executionId)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));
    }
}
