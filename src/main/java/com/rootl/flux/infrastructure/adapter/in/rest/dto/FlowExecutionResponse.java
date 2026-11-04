package com.rootl.flux.infrastructure.adapter.in.rest.dto;

import com.rootl.flux.domain.model.FlowExecution;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record FlowExecutionResponse(
        @Schema(description = "Identificador único da execução do fluxo", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
        String id,

        @Schema(description = "Identificador do fluxo de trabalho", example = "fluxo-processamento-pedidos")
        String flowId,

        @Schema(description = "Identificador do evento que disparou o fluxo", example = "550e8400-e29b-41d4-a716-446655440000")
        String triggerEventId,

        @Schema(description = "Estado atual da execução", example = "COMPLETED")
        String status,

        @Schema(description = "Data e hora de início da execução")
        Instant createdAt,

        @Schema(description = "Data e hora de conclusão da execução")
        Instant completedAt,

        @Schema(description = "Mensagem de erro caso a execução tenha falhado")
        String errorMessage
) {
    public static FlowExecutionResponse fromDomain(FlowExecution execution) {
        return new FlowExecutionResponse(
                execution.getId(),
                execution.getFlowId(),
                execution.getTriggerEventId().value(),
                execution.getStatus().name(),
                execution.getCreatedAt(),
                execution.getCompletedAt(),
                execution.getErrorMessage()
        );
    }
}
