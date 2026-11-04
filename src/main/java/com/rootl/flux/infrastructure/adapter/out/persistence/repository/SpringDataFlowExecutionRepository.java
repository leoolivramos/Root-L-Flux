package com.rootl.flux.infrastructure.adapter.out.persistence.repository;

import com.rootl.flux.infrastructure.adapter.out.persistence.entity.FlowExecutionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataFlowExecutionRepository extends JpaRepository<FlowExecutionJpaEntity, String> {
}
