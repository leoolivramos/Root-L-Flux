package com.rootl.flux.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "flux_events", indexes = {
        @Index(name = "idx_event_idempotency", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_event_correlation", columnList = "correlation_id")
})
public class EventJpaEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "event_type", length = 128, nullable = false)
    private String eventType;

    @Column(name = "source", length = 128, nullable = false)
    private String source;

    @Column(name = "idempotency_key", length = 128, unique = true)
    private String idempotencyKey;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "traceparent", length = 256)
    private String traceparent;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Lob
    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    public EventJpaEntity() {}

    public EventJpaEntity(String id, String eventType, String source, String idempotencyKey,
                          String correlationId, String traceparent, Instant timestamp,
                          String payload, String status) {
        this.id = id;
        this.eventType = eventType;
        this.source = source;
        this.idempotencyKey = idempotencyKey;
        this.correlationId = correlationId;
        this.traceparent = traceparent;
        this.timestamp = timestamp;
        this.payload = payload;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getEventType() {
        return eventType;
    }

    public String getSource() {
        return source;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getTraceparent() {
        return traceparent;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getPayload() {
        return payload;
    }

    public String getStatus() {
        return status;
    }
}
