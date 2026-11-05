package com.rootl.flux.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateFlowDefinitionRequest(
        @NotBlank(message = "O campo 'flowId' (identificador do fluxo) é obrigatório")
        @Schema(description = "Identificador único do fluxo", example = "fluxo-processamento-pedidos")
        String flowId,

        @NotBlank(message = "O campo 'name' (nome do fluxo) é obrigatório")
        @Schema(description = "Nome descritivo do fluxo de trabalho", example = "Fluxo de Processamento de Pedidos")
        String name,

        @NotBlank(message = "O campo 'triggerEventType' (tipo de evento disparador) é obrigatório")
        @Schema(description = "Tipo de evento que dispara automaticamente este fluxo", example = "pedido.criado")
        String triggerEventType,

        @NotEmpty(message = "O fluxo deve conter pelo menos uma etapa (steps)")
        @Valid
        @Schema(description = "Lista ordenada de etapas a serem executadas")
        List<StepDefinitionDto> steps
) {}
