package com.rootl.flux.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record StepDefinitionDto(
        @NotBlank(message = "O campo 'stepId' (identificador da etapa) é obrigatório")
        @Schema(description = "Identificador único da etapa dentro do fluxo", example = "validar-estoque")
        String stepId,

        @NotBlank(message = "O campo 'name' (nome da etapa) é obrigatório")
        @Schema(description = "Nome descritivo da etapa", example = "Validação de Estoque")
        String name,

        @PositiveOrZero(message = "A ordem da etapa (orderIndex) deve ser zero ou positiva")
        @Schema(description = "Ordem sequencial de execução", example = "1")
        int orderIndex,

        @Schema(description = "Tipo de ação da etapa", example = "VALIDACAO")
        String actionType,

        @Schema(description = "Número máximo de tentativas em caso de erro", example = "3")
        Integer maxAttempts
) {}
