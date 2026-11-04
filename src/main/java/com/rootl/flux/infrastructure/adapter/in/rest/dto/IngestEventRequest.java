package com.rootl.flux.infrastructure.adapter.in.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record IngestEventRequest(
        @NotBlank(message = "O campo 'type' (tipo do evento) é obrigatório")
        @Schema(description = "Identificador do tipo de evento", example = "pedido.criado")
        String type,

        @NotBlank(message = "O campo 'source' (origem do evento) é obrigatório")
        @Schema(description = "Sistema ou serviço de origem", example = "servico-pedidos")
        String source,

        @NotBlank(message = "O campo 'payload' (carga útil do evento) é obrigatório")
        @Schema(description = "Carga útil de negócio em formato JSON ou texto bruto", example = "{\"pedidoId\":\"123\",\"total\":99.90}")
        String payload,

        @Schema(description = "Chave opcional de idempotência para desduplicação de eventos", example = "idem-pedido-123")
        String idempotencyKey,

        @Schema(description = "Identificador opcional de correlação para rastreamento distribuído", example = "corr-abc-789")
        String correlationId,

        @Schema(description = "Cabeçalho de contexto distribuído W3C traceparent", example = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
        String traceparent
) {}
