package com.rootl.flux.application.port.out;

import com.rootl.flux.domain.model.FlowDefinition;

import java.util.List;
import java.util.Optional;

public interface FlowDefinitionRepositoryPort {
    FlowDefinition save(FlowDefinition flowDefinition);
    Optional<FlowDefinition> findById(String flowId);
    List<FlowDefinition> findByTriggerEventType(String triggerEventType);
    List<FlowDefinition> findAll();
}
