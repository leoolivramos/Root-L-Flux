package com.rootl.flux.infrastructure.adapter.in.rest;

import com.rootl.flux.application.port.in.ManageFlowDefinitionUseCase;
import com.rootl.flux.application.port.in.ManageFlowDefinitionUseCase.RegisterFlowCommand;
import com.rootl.flux.application.port.in.ManageFlowDefinitionUseCase.RegisterStepCommand;
import com.rootl.flux.domain.model.FlowDefinition;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.CreateFlowDefinitionRequest;
import com.rootl.flux.infrastructure.adapter.in.rest.dto.FlowDefinitionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/flows")
@Tag(name = "Definições de Fluxo", description = "Endpoints para cadastro e consulta de definições de workflows distribuídos")
public class FlowDefinitionController {

    private final ManageFlowDefinitionUseCase manageFlowDefinitionUseCase;

    public FlowDefinitionController(ManageFlowDefinitionUseCase manageFlowDefinitionUseCase) {
        this.manageFlowDefinitionUseCase = manageFlowDefinitionUseCase;
    }

    @PostMapping
    @Operation(summary = "Cadastrar uma nova definição de fluxo", description = "Registra um workflow com suas etapas sequenciais, tipos de ação e políticas de retry")
    @ApiResponse(responseCode = "201", description = "Definição de fluxo criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Parâmetros inválidos ou ausentes")
    public ResponseEntity<FlowDefinitionResponse> register(@Valid @RequestBody CreateFlowDefinitionRequest request) {
        List<RegisterStepCommand> stepCommands = request.steps().stream()
                .map(s -> new RegisterStepCommand(s.stepId(), s.name(), s.orderIndex(), s.actionType(), s.maxAttempts()))
                .toList();

        RegisterFlowCommand command = new RegisterFlowCommand(
                request.flowId(),
                request.name(),
                request.triggerEventType(),
                stepCommands
        );

        FlowDefinition saved = manageFlowDefinitionUseCase.register(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(FlowDefinitionResponse.fromDomain(saved));
    }

    @GetMapping
    @Operation(summary = "Listar todos os fluxos cadastrados", description = "Retorna todas as definições ativas no sistema")
    @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso")
    public ResponseEntity<List<FlowDefinitionResponse>> listAll() {
        List<FlowDefinitionResponse> list = manageFlowDefinitionUseCase.listAll().stream()
                .map(FlowDefinitionResponse::fromDomain)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar definição de fluxo por identificador", description = "Retorna detalhes e etapas de um fluxo")
    @ApiResponse(responseCode = "200", description = "Fluxo localizado")
    @ApiResponse(responseCode = "404", description = "Fluxo não localizado")
    public ResponseEntity<FlowDefinitionResponse> getById(@PathVariable("id") String id) {
        return manageFlowDefinitionUseCase.findById(id)
                .map(f -> ResponseEntity.ok(FlowDefinitionResponse.fromDomain(f)))
                .orElse(ResponseEntity.notFound().build());
    }
}
