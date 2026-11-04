package com.rootl.flux.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "flux_flow_executions", indexes = {
        @Index(name = "idx_exec_flow_id", columnList = "flow_id"),
        @Index(name = "idx_exec_status", columnList = "status")
})
public class FlowExecutionJpaEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "flow_id", length = 128, nullable = false)
    private String flowId;

    @Column(name = "trigger_event_id", length = 64, nullable = false)
    private String triggerEventId;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Version
    @Column(name = "version")
    private Long version;

    public FlowExecutionJpaEntity() {}

    public FlowExecutionJpaEntity(String id, String flowId, String triggerEventId, String status,
                                  Instant createdAt, Instant completedAt, String errorMessage, Long version) {
        this.id = id;
        this.flowId = flowId;
        this.triggerEventId = triggerEventId;
        this.status = status;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
        this.errorMessage = errorMessage;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getFlowId() {
        return flowId;
    }

    public String getTriggerEventId() {
        return triggerEventId;
    }

    public String getStatus() {
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
}
