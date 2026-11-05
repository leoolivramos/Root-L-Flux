package com.rootl.flux.domain.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class StepExecution implements Serializable {

    private final String id;
    private final String flowExecutionId;
    private final String stepId;
    private final int orderIndex;
    private ExecutionStatus status;
    private int attemptCount;
    private Instant startedAt;
    private Instant completedAt;
    private String output;
    private String errorMessage;

    public StepExecution(String id, String flowExecutionId, String stepId, int orderIndex,
                         ExecutionStatus status, int attemptCount, Instant startedAt,
                         Instant completedAt, String output, String errorMessage) {
        this.id = Objects.requireNonNull(id, "O identificador da execução da etapa não pode ser nulo");
        this.flowExecutionId = Objects.requireNonNull(flowExecutionId, "O identificador da execução do fluxo não pode ser nulo");
        this.stepId = Objects.requireNonNull(stepId, "O identificador da etapa (stepId) não pode ser nulo");
        this.orderIndex = orderIndex;
        this.status = status != null ? status : ExecutionStatus.RECEIVED;
        this.attemptCount = Math.max(1, attemptCount);
        this.startedAt = startedAt != null ? startedAt : Instant.now();
        this.completedAt = completedAt;
        this.output = output;
        this.errorMessage = errorMessage;
    }

    public static StepExecution create(String flowExecutionId, String stepId, int orderIndex) {
        return new StepExecution(
                UUID.randomUUID().toString(),
                flowExecutionId,
                stepId,
                orderIndex,
                ExecutionStatus.RECEIVED,
                1,
                Instant.now(),
                null,
                null,
                null
        );
    }

    public void start() {
        this.status = ExecutionStatus.RUNNING;
        this.startedAt = Instant.now();
    }

    public void complete(String output) {
        this.status = ExecutionStatus.COMPLETED;
        this.completedAt = Instant.now();
        this.output = output;
        this.errorMessage = null;
    }

    public void fail(String reason) {
        this.status = ExecutionStatus.FAILED;
        this.completedAt = Instant.now();
        this.errorMessage = reason;
    }

    public void incrementAttempt() {
        this.attemptCount++;
        this.status = ExecutionStatus.RETRYING;
        this.startedAt = Instant.now();
        this.errorMessage = null;
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

    public ExecutionStatus getStatus() {
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
