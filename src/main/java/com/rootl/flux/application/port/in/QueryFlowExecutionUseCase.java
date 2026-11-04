package com.rootl.flux.application.port.in;

import com.rootl.flux.domain.model.FlowExecution;
import java.util.Optional;

public interface QueryFlowExecutionUseCase {
    Optional<FlowExecution> getExecution(String executionId);
}
