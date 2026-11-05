package com.rootl.flux.application.service;

import com.rootl.flux.application.port.in.ExecuteFlowUseCase;
import com.rootl.flux.application.port.in.QueryFlowExecutionUseCase;
import com.rootl.flux.application.port.in.ReplayExecutionUseCase;
import com.rootl.flux.application.port.out.EventRepositoryPort;
import com.rootl.flux.application.port.out.FlowDefinitionRepositoryPort;
import com.rootl.flux.application.port.out.FlowExecutionRepositoryPort;
import com.rootl.flux.application.port.out.StepExecutionRepositoryPort;
import com.rootl.flux.domain.exception.EventValidationException;
import com.rootl.flux.domain.exception.FlowNotFoundException;
import com.rootl.flux.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FlowExecutionOrchestrator implements ExecuteFlowUseCase, QueryFlowExecutionUseCase, ReplayExecutionUseCase {

    private static final Logger log = LoggerFactory.getLogger(FlowExecutionOrchestrator.class);

    private final FlowExecutionRepositoryPort flowExecutionRepositoryPort;
    private final FlowDefinitionRepositoryPort flowDefinitionRepositoryPort;
    private final StepExecutionRepositoryPort stepExecutionRepositoryPort;
    private final EventRepositoryPort eventRepositoryPort;

    public FlowExecutionOrchestrator(FlowExecutionRepositoryPort flowExecutionRepositoryPort,
                                    FlowDefinitionRepositoryPort flowDefinitionRepositoryPort,
                                    StepExecutionRepositoryPort stepExecutionRepositoryPort,
                                    EventRepositoryPort eventRepositoryPort) {
        this.flowExecutionRepositoryPort = Objects.requireNonNull(flowExecutionRepositoryPort, "A porta do repositório de execuções não pode ser nula");
        this.flowDefinitionRepositoryPort = Objects.requireNonNull(flowDefinitionRepositoryPort, "A porta do repositório de definições de fluxo não pode ser nula");
        this.stepExecutionRepositoryPort = Objects.requireNonNull(stepExecutionRepositoryPort, "A porta do repositório de etapas não pode ser nula");
        this.eventRepositoryPort = Objects.requireNonNull(eventRepositoryPort, "A porta do repositório de eventos não pode ser nula");
    }

    @Override
    public FlowExecution execute(String flowId, EventId eventId) {
        if (flowId == null || flowId.isBlank()) {
            throw new EventValidationException("O identificador do fluxo (flowId) não pode estar em branco");
        }
        Event event = eventRepositoryPort.findById(eventId)
                .orElseThrow(() -> new EventValidationException("Evento não encontrado com o identificador: " + eventId));

        FlowDefinition flowDefinition = flowDefinitionRepositoryPort.findById(flowId)
                .orElseThrow(() -> new FlowNotFoundException("Fluxo não encontrado com o identificador: " + flowId));

        FlowExecution execution = FlowExecution.start(flowId, event.getId());
        execution = flowExecutionRepositoryPort.save(execution);

        log.info("[Orquestrador] Iniciando execução de fluxo id={} flowId={} triggerEventId={}",
                execution.getId(), flowId, event.getId().value());

        executeSteps(execution, flowDefinition, event);

        return flowExecutionRepositoryPort.save(execution);
    }

    @Override
    public Optional<FlowExecution> getExecution(String executionId) {
        return flowExecutionRepositoryPort.findById(executionId).map(exec -> {
            List<StepExecution> steps = stepExecutionRepositoryPort.findByFlowExecutionId(executionId);
            return new FlowExecution(
                    exec.getId(),
                    exec.getFlowId(),
                    exec.getTriggerEventId(),
                    exec.getStatus(),
                    exec.getCreatedAt(),
                    exec.getCompletedAt(),
                    exec.getErrorMessage(),
                    exec.getVersion(),
                    steps
            );
        });
    }

    @Override
    public FlowExecution replay(String executionId) {
        FlowExecution execution = getExecution(executionId)
                .orElseThrow(() -> new EventValidationException("Execução não encontrada para reprocessamento com ID: " + executionId));

        Event event = eventRepositoryPort.findById(execution.getTriggerEventId())
                .orElseThrow(() -> new EventValidationException("Evento disparador não localizado: " + execution.getTriggerEventId()));

        FlowDefinition flowDefinition = flowDefinitionRepositoryPort.findById(execution.getFlowId())
                .orElseThrow(() -> new FlowNotFoundException("Definição de fluxo não encontrada: " + execution.getFlowId()));

        log.info("[Orquestrador] Reprocessando execução de fluxo id={}", executionId);
        execution.replay();
        flowExecutionRepositoryPort.save(execution);

        executeSteps(execution, flowDefinition, event);

        return flowExecutionRepositoryPort.save(execution);
    }

    private void executeSteps(FlowExecution execution, FlowDefinition flowDefinition, Event event) {
        boolean allSuccess = true;

        for (StepDefinition stepDef : flowDefinition.getSteps()) {
            StepExecution stepExec = StepExecution.create(execution.getId(), stepDef.stepId(), stepDef.orderIndex());
            stepExec.start();

            try {
                // Simulação determinística de processamento da etapa do fluxo
                String result = String.format("{\"etapa\":\"%s\",\"status\":\"SUCESSO\",\"processadoEm\":\"%s\"}",
                        stepDef.stepId(), java.time.Instant.now());
                stepExec.complete(result);
                stepExecutionRepositoryPort.save(stepExec);
                execution.addStepExecution(stepExec);
                log.info("[Orquestrador] Etapa concluída com sucesso: stepId={} executionId={}", stepDef.stepId(), execution.getId());
            } catch (Exception ex) {
                log.error("[Orquestrador] Falha ao processar etapa: stepId={} erro={}", stepDef.stepId(), ex.getMessage());
                stepExec.fail(ex.getMessage());
                stepExecutionRepositoryPort.save(stepExec);
                execution.addStepExecution(stepExec);
                execution.fail("Falha na etapa '" + stepDef.stepId() + "': " + ex.getMessage());
                allSuccess = false;
                break;
            }
        }

        if (allSuccess) {
            execution.complete();
            log.info("[Orquestrador] Fluxo concluído com êxito: executionId={}", execution.getId());
        }
    }
}
