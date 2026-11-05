package com.rootl.flux.infrastructure.adapter.out.persistence.repository;

import com.rootl.flux.infrastructure.adapter.out.persistence.entity.FlowDefinitionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataFlowDefinitionRepository extends JpaRepository<FlowDefinitionJpaEntity, String> {
    List<FlowDefinitionJpaEntity> findByTriggerEventType(String triggerEventType);
}
