package com.rootl.flux.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class FlowExecution implements Serializable {

    private final String id;
    private final String flowId;
    private final EventId triggerEventId;
    private ExecutionStatus status;
    private final Instant createdAt;
    private Instant completedAt;
    private String errorMessage;
    private Long version;

    public FlowExecution(String id, String flowId, EventId triggerEventId, ExecutionStatus status,
                         Instant createdAt, Instant completedAt, String errorMessage, Long version) {
        this.id = Objects.requireNonNull(id, "O identificador da execução (id) não pode ser nulo");
        this.flowId = Objects.requireNonNull(flowId, "O identificador do fluxo (flowId) não pode ser nulo");
        this.triggerEventId = Objects.requireNonNull(triggerEventId, "O identificador do evento disparador (triggerEventId) não pode ser nulo");
        this.status = status != null ? status : ExecutionStatus.RECEIVED;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
        this.version = version != null ? version : 0L;
    }

    public static FlowExecution start(String flowId, EventId triggerEventId) {
        return new FlowExecution(
                UUID.randomUUID().toString(),
                flowId,
                triggerEventId,
                ExecutionStatus.RUNNING,
                Instant.now(),
                null,
                null,
                0L
        );
    }

    public void complete() {
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    public void fail(String reason) {
        this.status = ExecutionStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = reason;
    }

    public String getId() {
        return id;
    }

    public String getFlowId() {
        return flowId;
    }

    public EventId getTriggerEventId() {
        return triggerEventId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
