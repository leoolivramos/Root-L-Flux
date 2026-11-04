package com.rootl.flux.application.service;

import com.rootl.flux.application.port.in.IngestEventUseCase;
import com.rootl.flux.application.port.out.EventPublisherPort;
import com.rootl.flux.application.port.out.EventRepositoryPort;
import com.rootl.flux.domain.exception.DuplicateEventException;
import com.rootl.flux.domain.exception.EventValidationException;
import com.rootl.flux.domain.model.Event;
import com.rootl.flux.domain.model.EventMetadata;

import java.util.Objects;

public class EventIngestionService implements IngestEventUseCase {

    private final EventRepositoryPort eventRepositoryPort;
    private final EventPublisherPort eventPublisherPort;

    public EventIngestionService(EventRepositoryPort eventRepositoryPort, EventPublisherPort eventPublisherPort) {
        this.eventRepositoryPort = Objects.requireNonNull(eventRepositoryPort, "A porta do repositório de eventos não pode ser nula");
        this.eventPublisherPort = Objects.requireNonNull(eventPublisherPort, "A porta do publicador de eventos não pode ser nula");
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

        return savedEvent;
    }
}
