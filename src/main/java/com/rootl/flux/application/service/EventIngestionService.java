package com.rootl.flux.application.service;

import com.rootl.flux.application.port.in.ExecuteFlowUseCase;
import com.rootl.flux.application.port.in.IngestEventUseCase;
import com.rootl.flux.application.port.out.EventPublisherPort;
import com.rootl.flux.application.port.out.EventRepositoryPort;
import com.rootl.flux.application.port.out.FlowDefinitionRepositoryPort;
import com.rootl.flux.application.port.out.MetricsPort;
import com.rootl.flux.domain.exception.DuplicateEventException;
import com.rootl.flux.domain.exception.EventValidationException;
import com.rootl.flux.domain.model.Event;
import com.rootl.flux.domain.model.EventMetadata;
import com.rootl.flux.domain.model.FlowDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class EventIngestionService implements IngestEventUseCase {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionService.class);

    private final EventRepositoryPort eventRepositoryPort;
    private final EventPublisherPort eventPublisherPort;
    private final FlowDefinitionRepositoryPort flowDefinitionRepositoryPort;
    private final ExecuteFlowUseCase executeFlowUseCase;
    private final MetricsPort metricsPort;

    public EventIngestionService(EventRepositoryPort eventRepositoryPort,
                                 EventPublisherPort eventPublisherPort) {
        this(eventRepositoryPort, eventPublisherPort, null, null, null);
    }

    public EventIngestionService(EventRepositoryPort eventRepositoryPort,
                                 EventPublisherPort eventPublisherPort,
                                 FlowDefinitionRepositoryPort flowDefinitionRepositoryPort,
                                 ExecuteFlowUseCase executeFlowUseCase) {
        this(eventRepositoryPort, eventPublisherPort, flowDefinitionRepositoryPort, executeFlowUseCase, null);
    }

    public EventIngestionService(EventRepositoryPort eventRepositoryPort,
                                 EventPublisherPort eventPublisherPort,
                                 FlowDefinitionRepositoryPort flowDefinitionRepositoryPort,
                                 ExecuteFlowUseCase executeFlowUseCase,
                                 MetricsPort metricsPort) {
        this.eventRepositoryPort = Objects.requireNonNull(eventRepositoryPort, "A porta do repositório de eventos não pode ser nula");
        this.eventPublisherPort = Objects.requireNonNull(eventPublisherPort, "A porta do publicador de eventos não pode ser nula");
        this.flowDefinitionRepositoryPort = flowDefinitionRepositoryPort;
        this.executeFlowUseCase = executeFlowUseCase;
        this.metricsPort = metricsPort;
    }

    @Override
    public Event ingest(IngestEventCommand command) {
        if (command == null) {
            throw new EventValidationException("O comando de ingestão de evento (IngestEventCommand) não pode ser nulo");
        }
        if (command.type() == null || command.type().isBlank()) {
            throw new EventValidationException("O campo 'type' (tipo do evento) é obrigatório");
        }
        if (command.source() == null || command.source().isBlank()) {
            throw new EventValidationException("O campo 'source' (origem do evento) é obrigatório");
        }
        if (command.payload() == null || command.payload().isBlank()) {
            throw new EventValidationException("O campo 'payload' (conteúdo do evento) é obrigatório");
        }

        // Verificação de desduplicação e idempotência
        String idempotencyKey = command.idempotencyKey();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var existing = eventRepositoryPort.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                if (metricsPort != null) {
                    metricsPort.recordDuplicateEvent(command.type(), command.source());
                }
                throw new DuplicateEventException(idempotencyKey);
            }
        }

        EventMetadata metadata = EventMetadata.create(
                command.source(),
                idempotencyKey,
                command.correlationId(),
                command.traceparent()
        );

        Event event = Event.create(command.type(), metadata, command.payload());

        Event savedEvent = eventRepositoryPort.save(event);
        eventPublisherPort.publish(savedEvent);

        if (metricsPort != null) {
            metricsPort.recordEventIngested(savedEvent.getType(), savedEvent.getMetadata().source(), "SUCESSO");
        }

        // Disparo automático de fluxos cadastrados para este tipo de evento
        if (flowDefinitionRepositoryPort != null && executeFlowUseCase != null) {
            List<FlowDefinition> matchingFlows = flowDefinitionRepositoryPort.findByTriggerEventType(savedEvent.getType());
            for (FlowDefinition flowDef : matchingFlows) {
                try {
                    log.info("[Ingestão] Disparando fluxo automático '{}' para evento '{}'", flowDef.getFlowId(), savedEvent.getId().value());
                    executeFlowUseCase.execute(flowDef.getFlowId(), savedEvent.getId());
                } catch (Exception ex) {
                    log.error("[Ingestão] Erro ao disparar fluxo automático '{}': {}", flowDef.getFlowId(), ex.getMessage());
                }
            }
        }

        return savedEvent;
    }
}
