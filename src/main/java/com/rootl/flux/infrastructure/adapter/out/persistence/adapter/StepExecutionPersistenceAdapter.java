package com.rootl.flux.infrastructure.adapter.out.persistence.adapter;

import com.rootl.flux.application.port.out.StepExecutionRepositoryPort;
import com.rootl.flux.domain.model.ExecutionStatus;
import com.rootl.flux.domain.model.StepExecution;
import com.rootl.flux.infrastructure.adapter.out.persistence.entity.StepExecutionJpaEntity;
import com.rootl.flux.infrastructure.adapter.out.persistence.repository.SpringDataStepExecutionRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StepExecutionPersistenceAdapter implements StepExecutionRepositoryPort {

    private final SpringDataStepExecutionRepository repository;

    public StepExecutionPersistenceAdapter(SpringDataStepExecutionRepository repository) {
        this.repository = repository;
    }

    @Override
    public StepExecution save(StepExecution stepExecution) {
        StepExecutionJpaEntity entity = toEntity(stepExecution);
        StepExecutionJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<StepExecution> findByFlowExecutionId(String flowExecutionId) {
        return repository.findByFlowExecutionIdOrderByOrderIndexAsc(flowExecutionId).stream()
                .map(this::toDomain)
                .toList();
    }

    private StepExecutionJpaEntity toEntity(StepExecution domain) {
        return new StepExecutionJpaEntity(
                domain.getId(),
                domain.getFlowExecutionId(),
                domain.getStepId(),
                domain.getOrderIndex(),
                domain.getStatus().name(),
                domain.getAttemptCount(),
                domain.getStartedAt(),
                domain.getCompletedAt(),
                domain.getOutput(),
                domain.getErrorMessage()
        );
    }

    private StepExecution toDomain(StepExecutionJpaEntity entity) {
        return new StepExecution(
                entity.getId(),
                entity.getFlowExecutionId(),
                entity.getStepId(),
                entity.getOrderIndex(),
                ExecutionStatus.valueOf(entity.getStatus()),
                entity.getAttemptCount(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getOutput(),
                entity.getErrorMessage()
        );
    }
}
