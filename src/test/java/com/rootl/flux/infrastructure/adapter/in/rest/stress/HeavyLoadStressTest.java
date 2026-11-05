package com.rootl.flux.infrastructure.adapter.in.rest.stress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rootl.flux.application.port.in.ManageFlowDefinitionUseCase;
import com.rootl.flux.application.port.in.ManageFlowDefinitionUseCase.RegisterFlowCommand;
import com.rootl.flux.application.port.in.ManageFlowDefinitionUseCase.RegisterStepCommand;
import com.rootl.flux.application.port.out.EventRepositoryPort;
import com.rootl.flux.application.port.out.FlowExecutionRepositoryPort;
import com.rootl.flux.domain.model.EventId;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.IngestEventRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class HeavyLoadStressTest {

    private static final Logger log = LoggerFactory.getLogger(HeavyLoadStressTest.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepositoryPort eventRepositoryPort;

    @Autowired
    private ManageFlowDefinitionUseCase manageFlowDefinitionUseCase;

    @Autowired
    private FlowExecutionRepositoryPort flowExecutionRepositoryPort;

    @Test
    @DisplayName("Teste de Estresse: Alta concorrência de ingestão massiva de eventos sem perda de dados")
    void shouldHandleHighConcurrencyEventIngestionWithoutDataLoss() throws InterruptedException {
        int totalRequests = 250;
        int threadPoolSize = 30;

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(totalRequests);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalRequests; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startGate.await(); // Disparo sincronizado para maximizar estresse simultâneo
                    long t0 = System.currentTimeMillis();

                    IngestEventRequest request = new IngestEventRequest(
                            "carga.evento",
                            "servico-estresse",
                            "{\"seq\":" + index + ",\"dados\":\"carga-pesada\"}",
                            "chave-unica-carga-" + index + "-" + UUID.randomUUID(),
                            "corr-carga-" + index,
                            null
                    );

                    MvcResult result = mockMvc.perform(post("/api/v1/events")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andReturn();

                    long t1 = System.currentTimeMillis();
                    latencies.add(t1 - t0);

                    if (result.getResponse().getStatus() == 201) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception ex) {
                    errorCount.incrementAndGet();
                } finally {
                    endGate.countDown();
                }
            });
        }

        // Libera todos os threads simultaneamente
        startGate.countDown();
        boolean finished = endGate.await(45, TimeUnit.SECONDS);
        long totalDurationMs = System.currentTimeMillis() - startTime;
        executor.shutdown();

        assertTrue(finished, "O teste de estresse deve concluir dentro do tempo limite");
        assertEquals(totalRequests, successCount.get(), "Todas as 250 requisições concorrentes devem ser aceitas com 201 Created");
        assertEquals(0, errorCount.get(), "Nenhuma requisição deve falhar sob carga concorrente");

        double avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0.0);
        double rps = (totalRequests / (double) totalDurationMs) * 1000.0;

        log.info("=== RESULTADO DO ESTRESSE DE INGESTÃO ===");
        log.info("Total de requisições: {}", totalRequests);
        log.info("Duração total: {} ms", totalDurationMs);
        log.info("Throughput estimado: {} req/s", String.format("%.2f", rps));
        log.info("Latência média: {} ms", String.format("%.2f", avgLatency));
    }

    @Test
    @DisplayName("Teste de Estresse: Condição de corrida extrema em chave de idempotência com 40 threads simultâneos")
    void shouldEnforceStrictIdempotencyUnderMassiveRaceCondition() throws InterruptedException {
        int parallelAttempts = 40;
        String sharedKey = "chave-colisao-extrema-" + UUID.randomUUID();

        ExecutorService executor = Executors.newFixedThreadPool(parallelAttempts);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(parallelAttempts);

        AtomicInteger createdCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);

        for (int i = 0; i < parallelAttempts; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    startGate.await(); // Disparo no mesmo milissegundo

                    IngestEventRequest request = new IngestEventRequest(
                            "transacao.financeira",
                            "gateway-pagamento",
                            "{\"valor\":500.0,\"thread\":" + index + "}",
                            sharedKey, // Mesma chave compartilhada
                            "corr-compartilhada",
                            null
                    );

                    MvcResult result = mockMvc.perform(post("/api/v1/events")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    if (status == 201) {
                        createdCount.incrementAndGet();
                    } else if (status == 409) {
                        conflictCount.incrementAndGet();
                    } else {
                        log.warn("Status inesperado no thread de corrida de idempotência: {} - Mensagem: {}",
                                status, result.getResponse().getContentAsString());
                    }
                } catch (Exception ex) {
                    log.error("Erro no thread de corrida de idempotência: {}", ex.getMessage());
                } finally {
                    endGate.countDown();
                }
            });
        }

        startGate.countDown();
        endGate.await(20, TimeUnit.SECONDS);
        executor.shutdown();

        log.info("=== RESULTADO DA CONCORRÊNCIA DE IDEMPOTÊNCIA ===");
        log.info("Requisições criadas (201): {}", createdCount.get());
        log.info("Conflitos bloqueados (409): {}", conflictCount.get());

        assertEquals(1, createdCount.get(), "Exatamente UMA requisição deve vencer a corrida de idempotência");
        assertEquals(parallelAttempts - 1, conflictCount.get(), "Todas as outras 39 tentativas devem ser rejeitadas como 409 Conflict");
    }

    @Test
    @DisplayName("Teste de Estresse: Orquestração massiva de fluxos com etapas interdependentes")
    void shouldSustainHighThroughputWorkflowExecution() throws InterruptedException {
        String flowId = "fluxo-estresse-pesado-" + UUID.randomUUID();

        // 1. Cadastra fluxo com 3 etapas
        manageFlowDefinitionUseCase.register(new RegisterFlowCommand(
                flowId,
                "Workflow de Estresse",
                "gatilho.estresse",
                List.of(
                        new RegisterStepCommand("step-1-validacao", "Validação", 1, "VALIDACAO", 2),
                        new RegisterStepCommand("step-2-processamento", "Processamento", 2, "PROCESSAMENTO", 2),
                        new RegisterStepCommand("step-3-notificacao", "Notificação", 3, "NOTIFICACAO", 2)
                )
        ));

        int parallelExecutions = 30;
        ExecutorService executor = Executors.newFixedThreadPool(15);
        CountDownLatch endGate = new CountDownLatch(parallelExecutions);
        AtomicInteger successExecutions = new AtomicInteger(0);

        for (int i = 0; i < parallelExecutions; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    // Ingestão que dispara automaticamente o workflow
                    IngestEventRequest request = new IngestEventRequest(
                            "gatilho.estresse",
                            "produtor-estresse",
                            "{\"lote\":" + index + "}",
                            "chave-workflow-estresse-" + index + "-" + UUID.randomUUID(),
                            "corr-wf-" + index,
                            null
                    );

                    MvcResult result = mockMvc.perform(post("/api/v1/events")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andReturn();

                    if (result.getResponse().getStatus() == 201) {
                        successExecutions.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.error("Falha ao disparar workflow de estresse: {}", e.getMessage());
                } finally {
                    endGate.countDown();
                }
            });
        }

        endGate.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertEquals(parallelExecutions, successExecutions.get(),
                "Todos os 30 workflows concorrentes devem ser aceitos e disparados com sucesso");
    }
}
