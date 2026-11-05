package com.rootl.flux.application.port.in;

import com.rootl.flux.domain.model.FlowExecution;

public interface ReplayExecutionUseCase {
    FlowExecution replay(String executionId);
}
