package com.rootl.flux.application.port.in;

import com.rootl.flux.domain.model.FlowDefinition;

import java.util.List;
import java.util.Optional;

public interface ManageFlowDefinitionUseCase {

    FlowDefinition register(RegisterFlowCommand command);

    Optional<FlowDefinition> findById(String flowId);

    List<FlowDefinition> listAll();

    record RegisterFlowCommand(
            String flowId,
            String name,
            String triggerEventType,
            List<RegisterStepCommand> steps
    ) {}

    record RegisterStepCommand(
            String stepId,
            String name,
            int orderIndex,
            String actionType,
            Integer maxAttempts
    ) {}
}
