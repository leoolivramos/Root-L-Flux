package com.rootl.flux.infrastructure.adapter.in.rest.dto;

import com.rootl.flux.domain.model.Event;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record EventResponse(
        @Schema(description = "Identificador único do evento", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Tipo do evento", example = "pedido.criado")
        String type,

        @Schema(description = "Serviço ou aplicação de origem", example = "servico-pedidos")
        String source,

        @Schema(description = "Chave de idempotência", example = "idem-pedido-123")
        String idempotencyKey,

        @Schema(description = "Identificador de correlação", example = "corr-abc-789")
        String correlationId,

        @Schema(description = "Contexto distribuído W3C traceparent", example = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01")
        String traceparent,

        @Schema(description = "Data e hora de recebimento do evento em UTC")
        Instant timestamp,

        @Schema(description = "Carga útil de negócio (payload)")
        String payload,

        @Schema(description = "Estado atual do evento", example = "RECEIVED")
        String status
) {
    public static EventResponse fromDomain(Event event) {
        return new EventResponse(
                event.getId().value(),
                event.getType(),
                event.getMetadata().source(),
                event.getMetadata().idempotencyKey(),
                event.getMetadata().correlationId(),
                event.getMetadata().traceparent(),
                event.getMetadata().timestamp(),
                event.getPayload(),
                event.getStatus().name()
        );
    }
}
