package com.rootl.flux.domain.model;

import java.io.Serializable;
import java.util.Objects;

public class Event implements Serializable {

    private final EventId id;
    private final String type;
    private final EventMetadata metadata;
    private final String payload;
    private ExecutionStatus status;

    public Event(EventId id, String type, EventMetadata metadata, String payload, ExecutionStatus status) {
        this.id = Objects.requireNonNull(id, "O identificador do evento (id) não pode ser nulo");
        this.type = validateType(type);
        this.metadata = Objects.requireNonNull(metadata, "Os metadados do evento (metadata) não podem ser nulos");
        this.payload = Objects.requireNonNull(payload, "A carga útil (payload) não pode ser nula");
        this.status = status != null ? status : ExecutionStatus.RECEIVED;
    }

    public static Event create(String type, EventMetadata metadata, String payload) {
        return new Event(EventId.generate(), type, metadata, payload, ExecutionStatus.RECEIVED);
    }

    private static String validateType(String type) {
        Objects.requireNonNull(type, "O tipo do evento (type) não pode ser nulo");
        if (type.isBlank()) {
            throw new IllegalArgumentException("O tipo do evento (type) não pode estar em branco");
        }
        return type.trim();
    }

    public EventId getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public EventMetadata getMetadata() {
        return metadata;
    }

    public String getPayload() {
        return payload;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void markProcessing() {
        this.status = ExecutionStatus.RUNNING;
    }

    public void markCompleted() {
        this.status = ExecutionStatus.COMPLETED;
    }

    public void markFailed() {
        this.status = ExecutionStatus.FAILED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", status=" + status +
                '}';
    }
}
