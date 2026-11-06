package com.rootl.flux.infrastructure.adapter.out.observability;

import com.rootl.flux.application.port.out.MetricsPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;

/**
 * Adaptador de infraestrutura para registro e exportação de métricas via Micrometer e Prometheus.
 */
@Component
public class MicrometerMetricsAdapter implements MetricsPort {

    private final MeterRegistry meterRegistry;

    public MicrometerMetricsAdapter(MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "O MeterRegistry não pode ser nulo");
    }

    @Override
    public void recordEventIngested(String type, String source, String status) {
        Counter.builder("flux.events.ingested")
                .description("Total de eventos ingeridos na plataforma Root L Flux")
                .tag("type", sanitize(type))
                .tag("source", sanitize(source))
                .tag("status", sanitize(status))
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordDuplicateEvent(String type, String source) {
        Counter.builder("flux.events.duplicates")
                .description("Total de eventos rejeitados por chave de idempotência duplicada")
                .tag("type", sanitize(type))
                .tag("source", sanitize(source))
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordWorkflowExecution(String flowId, String status, Duration duration) {
        Timer.builder("flux.workflows.duration")
                .description("Tempo de execução de fluxos orquestrados")
                .tag("flowId", sanitize(flowId))
                .tag("status", sanitize(status))
                .register(meterRegistry)
                .record(duration != null ? duration : Duration.ZERO);

        Counter.builder("flux.workflows.executions")
                .description("Total de execuções de fluxo concluídas")
                .tag("flowId", sanitize(flowId))
                .tag("status", sanitize(status))
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordStepExecution(String actionType, String status, Duration duration) {
        Timer.builder("flux.steps.duration")
                .description("Tempo de execução de etapas individuais de fluxos")
                .tag("actionType", sanitize(actionType))
                .tag("status", sanitize(status))
                .register(meterRegistry)
                .record(duration != null ? duration : Duration.ZERO);

        Counter.builder("flux.steps.executions")
                .description("Total de etapas executadas")
                .tag("actionType", sanitize(actionType))
                .tag("status", sanitize(status))
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordDlqMessage(String reason) {
        Counter.builder("flux.dlq.messages")
                .description("Total de mensagens roteadas para a Dead Letter Queue")
                .tag("reason", truncate(reason, 60))
                .register(meterRegistry)
                .increment();
    }

    @Override
    public void recordDlqReprocessed(String status) {
        Counter.builder("flux.dlq.reprocessed")
                .description("Total de mensagens reprocessadas a partir da DLQ")
                .tag("status", sanitize(status))
                .register(meterRegistry)
                .increment();
    }

    private String sanitize(String value) {
        return value == null || value.isBlank() ? "desconhecido" : value;
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "desconhecido";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
