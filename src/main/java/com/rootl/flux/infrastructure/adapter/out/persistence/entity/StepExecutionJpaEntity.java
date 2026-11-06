package com.rootl.flux.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "flux_step_executions", indexes = {
        @Index(name = "idx_step_flow_exec_id", columnList = "flow_execution_id"),
        @Index(name = "idx_step_status", columnList = "status")
})
public class StepExecutionJpaEntity {

    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    @Column(name = "flow_execution_id", length = 64, nullable = false)
    private String flowExecutionId;

    @Column(name = "step_id", length = 64, nullable = false)
    private String stepId;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "status", length = 32, nullable = false)
    private String status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "output", columnDefinition = "TEXT")
    private String output;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    public StepExecutionJpaEntity() {}

    public StepExecutionJpaEntity(String id, String flowExecutionId, String stepId, int orderIndex,
                                  String status, int attemptCount, Instant startedAt,
                                  Instant completedAt, String output, String errorMessage) {
        this.id = id;
        this.flowExecutionId = flowExecutionId;
        this.stepId = stepId;
        this.orderIndex = orderIndex;
        this.status = status;
        this.attemptCount = attemptCount;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.output = output;
        this.errorMessage = errorMessage;
    }

    public String getId() {
        return id;
    }

    public String getFlowExecutionId() {
        return flowExecutionId;
    }

    public String getStepId() {
        return stepId;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public String getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public String getOutput() {
        return output;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
