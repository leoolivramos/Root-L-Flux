package com.rootl.flux.application.service;

import com.rootl.flux.application.port.in.ExecuteFlowUseCase;
import com.rootl.flux.application.port.in.QueryFlowExecutionUseCase;
import com.rootl.flux.application.port.out.EventRepositoryPort;
import com.rootl.flux.application.port.out.FlowExecutionRepositoryPort;
import com.rootl.flux.domain.exception.EventValidationException;
import com.rootl.flux.domain.model.EventId;
import com.rootl.flux.domain.model.FlowExecution;

import java.util.Objects;
import java.util.Optional;

public class FlowExecutionOrchestrator implements ExecuteFlowUseCase, QueryFlowExecutionUseCase {

    private final FlowExecutionRepositoryPort flowExecutionRepositoryPort;
    private final EventRepositoryPort eventRepositoryPort;

    public FlowExecutionOrchestrator(FlowExecutionRepositoryPort flowExecutionRepositoryPort,
                                    EventRepositoryPort eventRepositoryPort) {
        this.flowExecutionRepositoryPort = Objects.requireNonNull(flowExecutionRepositoryPort, "A porta do repositório de execução de fluxo não pode ser nula");
        this.eventRepositoryPort = Objects.requireNonNull(eventRepositoryPort, "A porta do repositório de eventos não pode ser nula");
    }

    @Override
    public FlowExecution execute(String flowId, EventId eventId) {
        if (flowId == null || flowId.isBlank()) {
            throw new EventValidationException("O identificador do fluxo (flowId) não pode estar em branco");
        }
        var event = eventRepositoryPort.findById(eventId)
                .orElseThrow(() -> new EventValidationException("Evento não encontrado com o identificador: " + eventId));

        FlowExecution execution = FlowExecution.start(flowId, event.getId());
        execution.complete(); // Execução síncrona no primeiro incremento funcional
        return flowExecutionRepositoryPort.save(execution);
    }

    @Override
    public Optional<FlowExecution> getExecution(String executionId) {
        return flowExecutionRepositoryPort.findById(executionId);
    }
}
