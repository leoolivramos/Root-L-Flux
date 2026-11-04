package com.rootl.flux.infrastructure.adapter.out.persistence.adapter;

import com.rootl.flux.application.port.out.FlowExecutionRepositoryPort;
import com.rootl.flux.domain.model.EventId;
import com.rootl.flux.domain.model.ExecutionStatus;
import com.rootl.flux.domain.model.FlowExecution;
import com.rootl.flux.infrastructure.adapter.out.persistence.entity.FlowExecutionJpaEntity;
import com.rootl.flux.infrastructure.adapter.out.persistence.repository.SpringDataFlowExecutionRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class FlowExecutionPersistenceAdapter implements FlowExecutionRepositoryPort {

    private final SpringDataFlowExecutionRepository repository;

    public FlowExecutionPersistenceAdapter(SpringDataFlowExecutionRepository repository) {
        this.repository = repository;
    }

    @Override
    public FlowExecution save(FlowExecution execution) {
        FlowExecutionJpaEntity entity = toEntity(execution);
        FlowExecutionJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<FlowExecution> findById(String id) {
        return repository.findById(id).map(this::toDomain);
    }

    private FlowExecutionJpaEntity toEntity(FlowExecution domain) {
        return new FlowExecutionJpaEntity(
                domain.getId(),
                domain.getFlowId(),
                domain.getTriggerEventId().value(),
                domain.getStatus().name(),
                domain.getCreatedAt(),
                domain.getCompletedAt(),
                domain.getErrorMessage(),
                domain.getVersion()
        );
    }

    private FlowExecution toDomain(FlowExecutionJpaEntity entity) {
        return new FlowExecution(
                entity.getId(),
                entity.getFlowId(),
                EventId.of(entity.getTriggerEventId()),
                ExecutionStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getCompletedAt(),
                entity.getErrorMessage(),
                entity.getVersion()
        );
    }
}
