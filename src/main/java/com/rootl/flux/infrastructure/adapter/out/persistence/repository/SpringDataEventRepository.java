package com.rootl.flux.infrastructure.adapter.out.persistence.repository;

import com.rootl.flux.infrastructure.adapter.out.persistence.entity.EventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpringDataEventRepository extends JpaRepository<EventJpaEntity, String> {
    Optional<EventJpaEntity> findByIdempotencyKey(String idempotencyKey);
}
