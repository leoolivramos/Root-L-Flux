package com.rootl.flux.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    @DisplayName("Deve instanciar um evento com status RECEIVED por padrão")
    void shouldCreateEventWithReceivedStatus() {
        EventMetadata metadata = EventMetadata.create("servico-pedidos", "idem-1", "corr-1", null);
        Event event = Event.create("pedido.criado", metadata, "{\"pedidoId\":\"10\"}");

        assertNotNull(event.getId());
        assertEquals("pedido.criado", event.getType());
        assertEquals(ExecutionStatus.RECEIVED, event.getStatus());
        assertEquals("idem-1", event.getMetadata().idempotencyKey());
        assertEquals("corr-1", event.getMetadata().correlationId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o tipo do evento for nulo ou em branco")
    void shouldThrowExceptionWhenTypeIsBlank() {
        EventMetadata metadata = EventMetadata.create("servico-pedidos", "idem-1", "corr-1", null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Event.create(" ", metadata, "{}"));
        assertEquals("O tipo do evento (type) não pode estar em branco", ex.getMessage());
    }

    @Test
    @DisplayName("Deve realizar transição de estados corretamente")
    void shouldTransitionStatusesCorrectly() {
        EventMetadata metadata = EventMetadata.create("servico-pedidos", "idem-1", "corr-1", null);
        Event event = Event.create("pedido.criado", metadata, "{}");

        assertEquals(ExecutionStatus.RECEIVED, event.getStatus());

        event.markProcessing();
        assertEquals(ExecutionStatus.RUNNING, event.getStatus());

        event.markCompleted();
        assertEquals(ExecutionStatus.COMPLETED, event.getStatus());
    }
}
