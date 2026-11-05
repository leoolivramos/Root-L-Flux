package com.rootl.flux.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flux_flow_definitions", indexes = {
        @Index(name = "idx_flow_trigger_type", columnList = "trigger_event_type")
})
public class FlowDefinitionJpaEntity {

    @Id
    @Column(name = "flow_id", length = 64, nullable = false)
    private String flowId;

    @Column(name = "name", length = 128, nullable = false)
    private String name;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "trigger_event_type", length = 128, nullable = false)
    private String triggerEventType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "flux_flow_steps", joinColumns = @JoinColumn(name = "flow_id"))
    @OrderBy("orderIndex ASC")
    private List<StepDefinitionEmbeddable> steps = new ArrayList<>();

    public FlowDefinitionJpaEntity() {}

    public FlowDefinitionJpaEntity(String flowId, String name, int version, String triggerEventType,
                                  List<StepDefinitionEmbeddable> steps) {
        this.flowId = flowId;
        this.name = name;
        this.version = version;
        this.triggerEventType = triggerEventType;
        this.steps = steps != null ? steps : new ArrayList<>();
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

    public List<StepDefinitionEmbeddable> getSteps() {
        return steps;
    }
}
