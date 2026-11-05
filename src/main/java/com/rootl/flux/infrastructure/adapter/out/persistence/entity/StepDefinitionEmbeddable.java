package com.rootl.flux.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class StepDefinitionEmbeddable {

    @Column(name = "step_id", length = 64, nullable = false)
    private String stepId;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "action_type", length = 64)
    private String actionType;

    @Column(name = "max_attempts")
    private int maxAttempts;

    @Column(name = "initial_delay_ms")
    private long initialDelayMs;

    @Column(name = "multiplier")
    private double multiplier;

    @Column(name = "max_delay_ms")
    private long maxDelayMs;

    public StepDefinitionEmbeddable() {}

    public StepDefinitionEmbeddable(String stepId, String name, int orderIndex, String actionType,
                                  int maxAttempts, long initialDelayMs, double multiplier, long maxDelayMs) {
        this.stepId = stepId;
        this.name = name;
        this.orderIndex = orderIndex;
        this.actionType = actionType;
        this.maxAttempts = maxAttempts;
        this.initialDelayMs = initialDelayMs;
        this.multiplier = multiplier;
        this.maxDelayMs = maxDelayMs;
    }

    public String getStepId() {
        return stepId;
    }

    public String getName() {
        return name;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public String getActionType() {
        return actionType;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getInitialDelayMs() {
        return initialDelayMs;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getMaxDelayMs() {
        return maxDelayMs;
    }
}
