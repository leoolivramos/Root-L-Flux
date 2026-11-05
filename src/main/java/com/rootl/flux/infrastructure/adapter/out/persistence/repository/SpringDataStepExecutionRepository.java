package com.rootl.flux.infrastructure.adapter.out.persistence.repository;

import com.rootl.flux.infrastructure.adapter.out.persistence.entity.StepExecutionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataStepExecutionRepository extends JpaRepository<StepExecutionJpaEntity, String> {
    List<StepExecutionJpaEntity> findByFlowExecutionIdOrderByOrderIndexAsc(String flowExecutionId);
}
