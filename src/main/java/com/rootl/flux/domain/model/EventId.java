package com.rootl.flux.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public record EventId(String value) implements Serializable {

    public EventId {
        Objects.requireNonNull(value, "O identificador do evento (EventId) não pode ser nulo");
        if (value.isBlank()) {
            throw new IllegalArgumentException("O identificador do evento (EventId) não pode estar em branco");
        }
    }

    public static EventId generate() {
        return new EventId(UUID.randomUUID().toString());
    }

    public static EventId of(String value) {
        return new EventId(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
