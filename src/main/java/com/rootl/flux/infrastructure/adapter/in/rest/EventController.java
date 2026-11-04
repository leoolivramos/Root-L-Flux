package com.rootl.flux.infrastructure.adapter.in.rest;

import com.rootl.flux.application.port.in.IngestEventUseCase;
import com.rootl.flux.application.port.out.EventRepositoryPort;
import com.rootl.flux.domain.model.Event;
import com.rootl.flux.domain.model.EventId;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.EventResponse;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.IngestEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Eventos", description = "Endpoints para ingestão e consulta de eventos distribuídos")
public class EventController {

    private final IngestEventUseCase ingestEventUseCase;
    private final EventRepositoryPort eventRepositoryPort;

    public EventController(IngestEventUseCase ingestEventUseCase, EventRepositoryPort eventRepositoryPort) {
        this.ingestEventUseCase = ingestEventUseCase;
        this.eventRepositoryPort = eventRepositoryPort;
    }

    @PostMapping
    @Operation(summary = "Ingerir um novo evento", description = "Recebe e persiste um evento externo, garantindo idempotência e validação de schema")
    @ApiResponse(responseCode = "201", description = "Evento recebido e persistido com sucesso")
    @ApiResponse(responseCode = "400", description = "Evento inválido ou campos obrigatórios ausentes")
    @ApiResponse(responseCode = "409", description = "Evento duplicado detectado pela chave de idempotência")
    public ResponseEntity<EventResponse> ingest(@Valid @RequestBody IngestEventRequest request) {
        IngestEventUseCase.IngestEventCommand command = new IngestEventUseCase.IngestEventCommand(
                request.type(),
                request.source(),
                request.payload(),
                request.idempotencyKey(),
                request.correlationId(),
                request.traceparent()
        );

        Event event = ingestEventUseCase.ingest(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(EventResponse.fromDomain(event));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar evento por identificador", description = "Recupera os detalhes e o estado de um evento pelo seu identificador único")
    @ApiResponse(responseCode = "200", description = "Evento localizado com sucesso")
    @ApiResponse(responseCode = "404", description = "Evento não localizado")
    public ResponseEntity<EventResponse> getById(@PathVariable("id") String id) {
        return eventRepositoryPort.findById(EventId.of(id))
                .map(event -> ResponseEntity.ok(EventResponse.fromDomain(event)))
                .orElse(ResponseEntity.notFound().build());
    }
}
