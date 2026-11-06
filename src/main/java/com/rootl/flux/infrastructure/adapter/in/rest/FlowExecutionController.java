package com.rootl.flux.infrastructure.adapter.in.rest;

import com.rootl.flux.application.port.in.ExecuteFlowUseCase;
import com.rootl.flux.application.port.in.QueryFlowExecutionUseCase;
import com.rootl.flux.application.port.in.ReplayExecutionUseCase;
import com.rootl.flux.domain.model.EventId;
import com.rootl.flux.domain.model.FlowExecution;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.FlowExecutionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/executions")
@Tag(name = "Execuções", description = "Endpoints para disparo, consulta e recuperação de execuções de fluxos")
public class FlowExecutionController {

    private final ExecuteFlowUseCase executeFlowUseCase;
    private final QueryFlowExecutionUseCase queryFlowExecutionUseCase;
    private final ReplayExecutionUseCase replayExecutionUseCase;

    public FlowExecutionController(ExecuteFlowUseCase executeFlowUseCase,
                                   QueryFlowExecutionUseCase queryFlowExecutionUseCase,
                                   ReplayExecutionUseCase replayExecutionUseCase) {
        this.executeFlowUseCase = executeFlowUseCase;
        this.queryFlowExecutionUseCase = queryFlowExecutionUseCase;
        this.replayExecutionUseCase = replayExecutionUseCase;
    }

    @GetMapping
    @Operation(summary = "Listar todas as execuções de fluxos", description = "Retorna histórico recente de execuções com suas respectivas etapas e status")
    @ApiResponse(responseCode = "200", description = "Lista de execuções recuperada com sucesso")
    public ResponseEntity<List<FlowExecutionResponse>> listAll() {
        List<FlowExecutionResponse> responses = queryFlowExecutionUseCase.getAllExecutions()
                .stream()
                .map(FlowExecutionResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/trigger")
    @Operation(summary = "Disparar execução de fluxo para um evento", description = "Inicia a execução de um fluxo específico a partir de um evento existente")
    @ApiResponse(responseCode = "201", description = "Execução do fluxo iniciada e concluída com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos fornecidos")
    @ApiResponse(responseCode = "404", description = "Fluxo ou evento não localizado")
    public ResponseEntity<FlowExecutionResponse> trigger(
            @RequestParam("flowId") String flowId,
            @RequestParam("eventId") String eventId) {
        FlowExecution execution = executeFlowUseCase.execute(flowId, EventId.of(eventId));
        return ResponseEntity.status(HttpStatus.CREATED).body(FlowExecutionResponse.fromDomain(execution));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar execução de fluxo por identificador", description = "Retorna o status atual, metadados e as etapas executadas")
    @ApiResponse(responseCode = "200", description = "Execução localizada com sucesso")
    @ApiResponse(responseCode = "404", description = "Execução não localizada")
    public ResponseEntity<FlowExecutionResponse> getById(@PathVariable("id") String id) {
        return queryFlowExecutionUseCase.getExecution(id)
                .map(exec -> ResponseEntity.ok(FlowExecutionResponse.fromDomain(exec)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/replay")
    @Operation(summary = "Reprocessar execução de fluxo (Flow Replay)", description = "Reinicia a execução de um fluxo a partir das etapas necessárias, permitindo recuperação de falhas")
    @ApiResponse(responseCode = "200", description = "Reprocessamento executado com sucesso")
    @ApiResponse(responseCode = "400", description = "Identificador de execução inválido")
    @ApiResponse(responseCode = "404", description = "Execução ou evento não localizado")
    public ResponseEntity<FlowExecutionResponse> replay(@PathVariable("id") String id) {
        FlowExecution replayed = replayExecutionUseCase.replay(id);
        return ResponseEntity.ok(FlowExecutionResponse.fromDomain(replayed));
    }
}
