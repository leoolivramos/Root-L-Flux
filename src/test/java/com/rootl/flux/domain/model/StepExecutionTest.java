package com.rootl.flux.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StepExecutionTest {

    @Test
    @DisplayName("Deve gerenciar o ciclo de vida e transições de estado da etapa")
    void shouldManageStepLifecycle() {
        StepExecution step = StepExecution.create("exec-1", "etapa-validacao", 1);

        assertEquals(ExecutionStatus.RECEIVED, step.getStatus());
        assertEquals(1, step.getAttemptCount());

        step.start();
        assertEquals(ExecutionStatus.RUNNING, step.getStatus());

        step.complete("{\"resultado\":\"OK\"}");
        assertEquals(ExecutionStatus.COMPLETED, step.getStatus());
        assertNotNull(step.getCompletedAt());
        assertEquals("{\"resultado\":\"OK\"}", step.getOutput());
    }

    @Test
    @DisplayName("Deve registrar falha e permitir incremento de tentativas")
    void shouldHandleFailureAndRetry() {
        StepExecution step = StepExecution.create("exec-1", "etapa-pagamento", 1);

        step.start();
        step.fail("Timeout no gateway de pagamento");

        assertEquals(ExecutionStatus.FAILED, step.getStatus());
        assertEquals("Timeout no gateway de pagamento", step.getErrorMessage());

        step.incrementAttempt();
        assertEquals(ExecutionStatus.RETRYING, step.getStatus());
        assertEquals(2, step.getAttemptCount());
        assertNull(step.getErrorMessage());
    }
}
