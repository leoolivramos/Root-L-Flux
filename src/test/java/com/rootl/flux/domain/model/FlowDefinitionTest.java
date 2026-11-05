package com.rootl.flux.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlowDefinitionTest {

    @Test
    @DisplayName("Deve instanciar definição de fluxo com etapas ordenadas")
    void shouldCreateFlowDefinitionWithSortedSteps() {
        StepDefinition step2 = StepDefinition.of("passo-2", "Segundo Passo", 2);
        StepDefinition step1 = StepDefinition.of("passo-1", "Primeiro Passo", 1);

        FlowDefinition flow = FlowDefinition.create(
                "fluxo-pedidos",
                "Fluxo de Pedidos",
                "pedido.criado",
                List.of(step2, step1)
        );

        assertNotNull(flow);
        assertEquals("fluxo-pedidos", flow.getFlowId());
        assertEquals("pedido.criado", flow.getTriggerEventType());
        assertEquals(2, flow.getSteps().size());
        assertEquals("passo-1", flow.getSteps().get(0).stepId(), "As etapas devem estar ordenadas por orderIndex");
        assertEquals("passo-2", flow.getSteps().get(1).stepId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando campos obrigatórios forem inválidos")
    void shouldThrowExceptionWhenMandatoryFieldsAreBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                FlowDefinition.create("", "Nome", "evento.tipo", List.of()));

        assertThrows(IllegalArgumentException.class, () ->
                FlowDefinition.create("id", " ", "evento.tipo", List.of()));

        assertThrows(IllegalArgumentException.class, () ->
                FlowDefinition.create("id", "Nome", " ", List.of()));
    }
}
