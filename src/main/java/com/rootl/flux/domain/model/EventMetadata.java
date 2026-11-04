package com.rootl.flux.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EventMetadata(
        String source,
        Instant timestamp,
        String correlationId,
        String idempotencyKey,
        String traceparent
) implements Serializable {

    public EventMetadata {
        Objects.requireNonNull(source, "A origem do evento (source) não pode ser nula");
        Objects.requireNonNull(timestamp, "O timestamp do evento não pode ser nulo");
        if (source.isBlank()) {
            throw new IllegalArgumentException("A origem do evento (source) não pode estar em branco");
        }
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            idempotencyKey = correlationId;
        }
    }

    public static EventMetadata create(String source, String idempotencyKey, String correlationId, String traceparent) {
        return new EventMetadata(
                source,
                Instant.now(),
                correlationId,
                idempotencyKey,
                traceparent
        );
    }
}
