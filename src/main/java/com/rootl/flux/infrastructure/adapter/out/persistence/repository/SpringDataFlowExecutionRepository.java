package com.rootl.flux.infrastructure.adapter.out.persistence.repository;

import com.rootl.flux.infrastructure.adapter.out.persistence.entity.FlowExecutionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataFlowExecutionRepository extends JpaRepository<FlowExecutionJpaEntity, String> {
    List<FlowExecutionJpaEntity> findAllByOrderByCreatedAtDesc();
}
