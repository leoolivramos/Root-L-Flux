package com.rootl.flux.domain.model;

import java.io.Serializable;
import java.util.Objects;

public record StepDefinition(
        String stepId,
        String name,
        int orderIndex,
        String actionType,
        RetryPolicy retryPolicy
) implements Serializable {

    public StepDefinition {
        Objects.requireNonNull(stepId, "O identificador da etapa (stepId) não pode ser nulo");
        Objects.requireNonNull(name, "O nome da etapa não pode ser nulo");
        if (stepId.isBlank()) {
            throw new IllegalArgumentException("O identificador da etapa (stepId) não pode estar em branco");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("O nome da etapa não pode estar em branco");
        }
        if (orderIndex < 0) {
            throw new IllegalArgumentException("A ordem da etapa (orderIndex) não pode ser negativa");
        }
        if (actionType == null || actionType.isBlank()) {
            actionType = "EXECUCAO";
        }
        if (retryPolicy == null) {
            retryPolicy = RetryPolicy.padrao();
        }
    }

    public static StepDefinition of(String stepId, String name, int orderIndex) {
        return new StepDefinition(stepId, name, orderIndex, "EXECUCAO", RetryPolicy.padrao());
    }

    public static StepDefinition of(String stepId, String name, int orderIndex, String actionType, RetryPolicy retryPolicy) {
        return new StepDefinition(stepId, name, orderIndex, actionType, retryPolicy);
    }
}
