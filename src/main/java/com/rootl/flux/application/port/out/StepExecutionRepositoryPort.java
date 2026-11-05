package com.rootl.flux.application.port.out;

import com.rootl.flux.domain.model.StepExecution;

import java.util.List;

public interface StepExecutionRepositoryPort {
    StepExecution save(StepExecution stepExecution);
    List<StepExecution> findByFlowExecutionId(String flowExecutionId);
}
