package com.rootl.flux.domain.model;

import java.io.Serializable;
import java.util.*;

public class FlowDefinition implements Serializable {

    private final String flowId;
    private final String name;
    private final int version;
    private final String triggerEventType;
    private final List<StepDefinition> steps;

    public FlowDefinition(String flowId, String name, int version, String triggerEventType, List<StepDefinition> steps) {
        this.flowId = Objects.requireNonNull(flowId, "O identificador do fluxo (flowId) não pode ser nulo");
        this.name = Objects.requireNonNull(name, "O nome do fluxo não pode ser nulo");
        this.triggerEventType = Objects.requireNonNull(triggerEventType, "O tipo de evento disparador (triggerEventType) não pode ser nulo");

        if (flowId.isBlank()) {
            throw new IllegalArgumentException("O identificador do fluxo (flowId) não pode estar em branco");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("O nome do fluxo não pode estar em branco");
        }
        if (triggerEventType.isBlank()) {
            throw new IllegalArgumentException("O tipo de evento disparador (triggerEventType) não pode estar em branco");
        }
        if (version < 1) {
            throw new IllegalArgumentException("A versão do fluxo deve ser no mínimo 1");
        }
        this.version = version;

        List<StepDefinition> sortedSteps = new ArrayList<>(steps != null ? steps : List.of());
        sortedSteps.sort(Comparator.comparingInt(StepDefinition::orderIndex));
        this.steps = Collections.unmodifiableList(sortedSteps);
    }

    public static FlowDefinition create(String flowId, String name, String triggerEventType, List<StepDefinition> steps) {
        return new FlowDefinition(flowId, name, 1, triggerEventType, steps);
    }

    public String getFlowId() {
        return flowId;
    }

    public String getName() {
        return name;
    }

    public int getVersion() {
        return version;
    }

    public String getTriggerEventType() {
        return triggerEventType;
    }

    public List<StepDefinition> getSteps() {
        return steps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowDefinition that = (FlowDefinition) o;
        return version == that.version && Objects.equals(flowId, that.flowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowId, version);
    }
}
