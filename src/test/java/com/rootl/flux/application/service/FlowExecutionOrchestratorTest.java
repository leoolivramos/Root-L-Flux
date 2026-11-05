package com.rootl.flux.application.service;

import com.rootl.flux.application.port.out.EventRepositoryPort;
import com.rootl.flux.application.port.out.FlowDefinitionRepositoryPort;
import com.rootl.flux.application.port.out.FlowExecutionRepositoryPort;
import com.rootl.flux.application.port.out.StepExecutionRepositoryPort;
import com.rootl.flux.domain.exception.FlowNotFoundException;
import com.rootl.flux.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlowExecutionOrchestratorTest {

    @Mock
    private FlowExecutionRepositoryPort flowExecutionRepositoryPort;

    @Mock
    private FlowDefinitionRepositoryPort flowDefinitionRepositoryPort;

    @Mock
    private StepExecutionRepositoryPort stepExecutionRepositoryPort;

    @Mock
    private EventRepositoryPort eventRepositoryPort;

    private FlowExecutionOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new FlowExecutionOrchestrator(
                flowExecutionRepositoryPort,
                flowDefinitionRepositoryPort,
                stepExecutionRepositoryPort,
                eventRepositoryPort
        );
    }

    @Test
    @DisplayName("Deve orquestrar execução de fluxo com suas etapas com sucesso")
    void shouldOrchestrateFlowExecutionSuccessfully() {
        EventId eventId = EventId.generate();
        Event event = Event.create("pedido.criado", EventMetadata.create("origem", "idem", "corr", null), "{}");

        StepDefinition step1 = StepDefinition.of("validar", "Validar", 1);
        StepDefinition step2 = StepDefinition.of("notificar", "Notificar", 2);
        FlowDefinition flow = FlowDefinition.create("fluxo-pedidos", "Fluxo Pedidos", "pedido.criado", List.of(step1, step2));

        when(eventRepositoryPort.findById(eventId)).thenReturn(Optional.of(event));
        when(flowDefinitionRepositoryPort.findById("fluxo-pedidos")).thenReturn(Optional.of(flow));
        when(flowExecutionRepositoryPort.save(any(FlowExecution.class))).thenAnswer(i -> i.getArgument(0));

        FlowExecution execution = orchestrator.execute("fluxo-pedidos", eventId);

        assertNotNull(execution);
        assertEquals(ExecutionStatus.COMPLETED, execution.getStatus());
        assertEquals("fluxo-pedidos", execution.getFlowId());
        assertEquals(2, execution.getStepExecutions().size());

        verify(stepExecutionRepositoryPort, times(2)).save(any(StepExecution.class));
        verify(flowExecutionRepositoryPort, atLeastOnce()).save(any(FlowExecution.class));
    }

    @Test
    @DisplayName("Deve lançar FlowNotFoundException quando o fluxo não existir")
    void shouldThrowExceptionWhenFlowNotFound() {
        EventId eventId = EventId.generate();
        Event event = Event.create("pedido.criado", EventMetadata.create("origem", "idem", "corr", null), "{}");

        when(eventRepositoryPort.findById(eventId)).thenReturn(Optional.of(event));
        when(flowDefinitionRepositoryPort.findById("fluxo-inexistente")).thenReturn(Optional.empty());

        assertThrows(FlowNotFoundException.class, () -> orchestrator.execute("fluxo-inexistente", eventId));
    }
}
