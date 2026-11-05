package com.rootl.flux.infrastructure.adapter.in.rest.dto;

import com.rootl.flux.domain.model.FlowDefinition;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record FlowDefinitionResponse(
        @Schema(description = "Identificador único do fluxo", example = "fluxo-processamento-pedidos")
        String flowId,

        @Schema(description = "Nome descritivo do fluxo", example = "Fluxo de Processamento de Pedidos")
        String name,

        @Schema(description = "Versão atual do fluxo", example = "1")
        int version,

        @Schema(description = "Tipo de evento que dispara este fluxo", example = "pedido.criado")
        String triggerEventType,

        @Schema(description = "Etapas configuradas para o fluxo")
        List<StepDefinitionDto> steps
) {
    public static FlowDefinitionResponse fromDomain(FlowDefinition domain) {
        List<StepDefinitionDto> stepDtos = domain.getSteps().stream()
                .map(s -> new StepDefinitionDto(
                        s.stepId(),
                        s.name(),
                        s.orderIndex(),
                        s.actionType(),
                        s.retryPolicy().maxAttempts()
                ))
                .toList();

        return new FlowDefinitionResponse(
                domain.getFlowId(),
                domain.getName(),
                domain.getVersion(),
                domain.getTriggerEventType(),
                stepDtos
        );
    }
}
