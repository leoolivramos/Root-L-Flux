package com.rootl.flux.application.service;

import com.rootl.flux.application.port.in.IngestEventUseCase.IngestEventCommand;
import com.rootl.flux.application.port.out.EventPublisherPort;
import com.rootl.flux.application.port.out.EventRepositoryPort;
import com.rootl.flux.domain.exception.DuplicateEventException;
import com.rootl.flux.domain.exception.EventValidationException;
import com.rootl.flux.domain.model.Event;
import com.rootl.flux.domain.model.EventMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventIngestionServiceTest {

    @Mock
    private EventRepositoryPort eventRepositoryPort;

    @Mock
    private EventPublisherPort eventPublisherPort;

    private EventIngestionService service;

    @BeforeEach
    void setUp() {
        service = new EventIngestionService(eventRepositoryPort, eventPublisherPort);
    }

    @Test
    @DisplayName("Deve ingerir evento com sucesso e publicar no broker")
    void shouldIngestAndPublishEvent() {
        IngestEventCommand command = new IngestEventCommand(
                "pagamento.recebido",
                "gateway-pagamento",
                "{\"valor\":150.0}",
                "chave-123",
                "corr-123",
                null
        );

        when(eventRepositoryPort.findByIdempotencyKey("chave-123")).thenReturn(Optional.empty());
        when(eventRepositoryPort.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event ingested = service.ingest(command);

        assertNotNull(ingested);
        assertEquals("pagamento.recebido", ingested.getType());
        assertEquals("chave-123", ingested.getMetadata().idempotencyKey());

        verify(eventRepositoryPort, times(1)).save(any(Event.class));
        verify(eventPublisherPort, times(1)).publish(any(Event.class));
    }

    @Test
    @DisplayName("Deve rejeitar evento duplicado com base na chave de idempotência")
    void shouldRejectDuplicateEvent() {
        IngestEventCommand command = new IngestEventCommand(
                "pagamento.recebido",
                "gateway-pagamento",
                "{\"valor\":150.0}",
                "chave-duplicada",
                "corr-123",
                null
        );

        Event existing = Event.create("pagamento.recebido", EventMetadata.create("gateway-pagamento", "chave-duplicada", "corr-123", null), "{}");
        when(eventRepositoryPort.findByIdempotencyKey("chave-duplicada")).thenReturn(Optional.of(existing));

        DuplicateEventException ex = assertThrows(DuplicateEventException.class, () -> service.ingest(command));
        assertTrue(ex.getMessage().contains("chave-duplicada"));
        verify(eventRepositoryPort, never()).save(any());
        verify(eventPublisherPort, never()).publish(any());
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios do comando")
    void shouldValidateRequiredFields() {
        IngestEventCommand missingType = new IngestEventCommand(
                "",
                "origem",
                "{}",
                null,
                null,
                null
        );

        EventValidationException ex = assertThrows(EventValidationException.class, () -> service.ingest(missingType));
        assertEquals("O campo 'type' (tipo do evento) é obrigatório", ex.getMessage());
    }
}
