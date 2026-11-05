package com.rootl.flux.infrastructure.adapter.in.rest.dto;

import com.rootl.flux.domain.model.StepExecution;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record StepExecutionResponse(
        @Schema(description = "Identificador único da execução da etapa", example = "a2d12e6b-6b83-4a6c-941e-6dc31dbabef0")
        String id,

        @Schema(description = "Identificador da etapa", example = "validar-estoque")
        String stepId,

        @Schema(description = "Ordem de execução da etapa", example = "1")
        int orderIndex,

        @Schema(description = "Estado da etapa", example = "COMPLETED")
        String status,

        @Schema(description = "Tentativa atual de execução", example = "1")
        int attemptCount,

        @Schema(description = "Horário de início da etapa")
        Instant startedAt,

        @Schema(description = "Horário de conclusão da etapa")
        Instant completedAt,

        @Schema(description = "Resultado/saída da etapa em formato JSON")
        String output,

        @Schema(description = "Mensagem de erro caso a etapa tenha falhado")
        String errorMessage
) {
    public static StepExecutionResponse fromDomain(StepExecution domain) {
        return new StepExecutionResponse(
                domain.getId(),
                domain.getStepId(),
                domain.getOrderIndex(),
                domain.getStatus().name(),
                domain.getAttemptCount(),
                domain.getStartedAt(),
                domain.getCompletedAt(),
                domain.getOutput(),
                domain.getErrorMessage()
        );
    }
}
