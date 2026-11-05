package com.rootl.flux.infrastructure.adapter.out.persistence.adapter;

import com.rootl.flux.application.port.out.FlowDefinitionRepositoryPort;
import com.rootl.flux.domain.model.FlowDefinition;
import com.rootl.flux.domain.model.RetryPolicy;
import com.rootl.flux.domain.model.StepDefinition;
import com.rootl.flux.infrastructure.adapter.out.persistence.entity.FlowDefinitionJpaEntity;
import com.rootl.flux.infrastructure.adapter.out.persistence.entity.StepDefinitionEmbeddable;
import com.rootl.flux.infrastructure.adapter.out.persistence.repository.SpringDataFlowDefinitionRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class FlowDefinitionPersistenceAdapter implements FlowDefinitionRepositoryPort {

    private final SpringDataFlowDefinitionRepository repository;

    public FlowDefinitionPersistenceAdapter(SpringDataFlowDefinitionRepository repository) {
        this.repository = repository;
    }

    @Override
    public FlowDefinition save(FlowDefinition flowDefinition) {
        FlowDefinitionJpaEntity entity = toEntity(flowDefinition);
        FlowDefinitionJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<FlowDefinition> findById(String flowId) {
        return repository.findById(flowId).map(this::toDomain);
    }

    @Override
    public List<FlowDefinition> findByTriggerEventType(String triggerEventType) {
        return repository.findByTriggerEventType(triggerEventType).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<FlowDefinition> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private FlowDefinitionJpaEntity toEntity(FlowDefinition domain) {
        List<StepDefinitionEmbeddable> stepEmbeddables = domain.getSteps().stream()
                .map(s -> new StepDefinitionEmbeddable(
                        s.stepId(),
                        s.name(),
                        s.orderIndex(),
                        s.actionType(),
                        s.retryPolicy().maxAttempts(),
                        s.retryPolicy().initialDelayMs(),
                        s.retryPolicy().multiplier(),
                        s.retryPolicy().maxDelayMs()
                ))
                .toList();

        return new FlowDefinitionJpaEntity(
                domain.getFlowId(),
                domain.getName(),
                domain.getVersion(),
                domain.getTriggerEventType(),
                stepEmbeddables
        );
    }

    private FlowDefinition toDomain(FlowDefinitionJpaEntity entity) {
        List<StepDefinition> steps = entity.getSteps().stream()
                .map(s -> new StepDefinition(
                        s.getStepId(),
                        s.getName(),
                        s.getOrderIndex(),
                        s.getActionType(),
                        new RetryPolicy(
                                Math.max(1, s.getMaxAttempts()),
                                Math.max(0L, s.getInitialDelayMs()),
                                Math.max(1.0, s.getMultiplier()),
                                Math.max(s.getInitialDelayMs(), s.getMaxDelayMs())
                        )
                ))
                .toList();

        return new FlowDefinition(
                entity.getFlowId(),
                entity.getName(),
                entity.getVersion(),
                entity.getTriggerEventType(),
                steps
        );
    }
}
