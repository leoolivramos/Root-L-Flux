package com.rootl.flux.application.service;

import com.rootl.flux.application.port.in.ManageFlowDefinitionUseCase;
import com.rootl.flux.application.port.out.FlowDefinitionRepositoryPort;
import com.rootl.flux.domain.exception.EventValidationException;
import com.rootl.flux.domain.model.FlowDefinition;
import com.rootl.flux.domain.model.RetryPolicy;
import com.rootl.flux.domain.model.StepDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FlowDefinitionService implements ManageFlowDefinitionUseCase {

    private final FlowDefinitionRepositoryPort repository;

    public FlowDefinitionService(FlowDefinitionRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository, "A porta do repositório de definições de fluxo não pode ser nula");
    }

    @Override
    public FlowDefinition register(RegisterFlowCommand command) {
        if (command == null) {
            throw new EventValidationException("O comando de registro do fluxo não pode ser nulo");
        }
        if (command.flowId() == null || command.flowId().isBlank()) {
            throw new EventValidationException("O identificador do fluxo (flowId) é obrigatório");
        }
        if (command.name() == null || command.name().isBlank()) {
            throw new EventValidationException("O nome do fluxo é obrigatório");
        }
        if (command.triggerEventType() == null || command.triggerEventType().isBlank()) {
            throw new EventValidationException("O tipo de evento disparador (triggerEventType) é obrigatório");
        }

        List<StepDefinition> steps = new ArrayList<>();
        if (command.steps() != null) {
            for (RegisterStepCommand stepCmd : command.steps()) {
                RetryPolicy retryPolicy = (stepCmd.maxAttempts() != null && stepCmd.maxAttempts() > 0)
                        ? new RetryPolicy(stepCmd.maxAttempts(), 1000L, 2.0, 10000L)
                        : RetryPolicy.padrao();

                steps.add(StepDefinition.of(
                        stepCmd.stepId(),
                        stepCmd.name(),
                        stepCmd.orderIndex(),
                        stepCmd.actionType(),
                        retryPolicy
                ));
            }
        }

        FlowDefinition flowDefinition = FlowDefinition.create(
                command.flowId(),
                command.name(),
                command.triggerEventType(),
                steps
        );

        return repository.save(flowDefinition);
    }

    @Override
    public Optional<FlowDefinition> findById(String flowId) {
        return repository.findById(flowId);
    }

    @Override
    public List<FlowDefinition> listAll() {
        return repository.findAll();
    }
}
