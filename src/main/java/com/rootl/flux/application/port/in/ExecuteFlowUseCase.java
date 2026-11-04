package com.rootl.flux.application.port.in;

import com.rootl.flux.domain.model.EventId;
import com.rootl.flux.domain.model.FlowExecution;

public interface ExecuteFlowUseCase {
    FlowExecution execute(String flowId, EventId eventId);
}
