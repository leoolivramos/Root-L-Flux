package com.rootl.flux.infrastructure.adapter.out.persistence.adapter;

import com.rootl.flux.application.port.out.EventRepositoryPort;
import com.rootl.flux.domain.model.Event;
import com.rootl.flux.domain.model.EventId;
import com.rootl.flux.domain.model.EventMetadata;
import com.rootl.flux.domain.model.ExecutionStatus;
import com.rootl.flux.infrastructure.adapter.out.persistence.entity.EventJpaEntity;
import com.rootl.flux.infrastructure.adapter.out.persistence.repository.SpringDataEventRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EventPersistenceAdapter implements EventRepositoryPort {

    private final SpringDataEventRepository repository;

    public EventPersistenceAdapter(SpringDataEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public Event save(Event event) {
        EventJpaEntity entity = toEntity(event);
        EventJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Event> findById(EventId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Event> findByIdempotencyKey(String idempotencyKey) {
        return repository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
    }

    private EventJpaEntity toEntity(Event event) {
        return new EventJpaEntity(
                event.getId().value(),
                event.getType(),
                event.getMetadata().source(),
                event.getMetadata().idempotencyKey(),
                event.getMetadata().correlationId(),
                event.getMetadata().traceparent(),
                event.getMetadata().timestamp(),
                event.getPayload(),
                event.getStatus().name()
        );
    }

    private Event toDomain(EventJpaEntity entity) {
        EventMetadata metadata = new EventMetadata(
                entity.getSource(),
                entity.getTimestamp(),
                entity.getCorrelationId(),
                entity.getIdempotencyKey(),
                entity.getTraceparent()
        );
        return new Event(
                EventId.of(entity.getId()),
                entity.getEventType(),
                metadata,
                entity.getPayload(),
                ExecutionStatus.valueOf(entity.getStatus())
        );
    }
}
