package com.rootl.flux.application.port.out;

import com.rootl.flux.domain.model.Event;
import com.rootl.flux.domain.model.EventId;

import java.util.Optional;

public interface EventRepositoryPort {
    Event save(Event event);
    Optional<Event> findById(EventId id);
    Optional<Event> findByIdempotencyKey(String idempotencyKey);
}
