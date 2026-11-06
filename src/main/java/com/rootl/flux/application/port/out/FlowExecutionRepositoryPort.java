package com.rootl.flux.application.port.out;

import com.rootl.flux.domain.model.FlowExecution;
import java.util.List;
import java.util.Optional;

public interface FlowExecutionRepositoryPort {
    FlowExecution save(FlowExecution execution);
    Optional<FlowExecution> findById(String id);
    List<FlowExecution> findAll();
}
