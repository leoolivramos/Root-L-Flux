package com.rootl.flux.infrastructure.adapter.in.observability;

import com.rootl.flux.application.port.out.FlowDefinitionRepositoryPort;
import com.rootl.flux.infrastructure.adapter.in.messaging.kafka.DlqMessageStore;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Indicador customizado de integridade e estado operacional do Root L Flux para o Spring Boot Actuator.
 */
@Component("flux")
public class FluxHealthIndicator implements HealthIndicator {

    private final DlqMessageStore dlqMessageStore;
    private final FlowDefinitionRepositoryPort flowDefinitionRepositoryPort;

    public FluxHealthIndicator(DlqMessageStore dlqMessageStore,
                               FlowDefinitionRepositoryPort flowDefinitionRepositoryPort) {
        this.dlqMessageStore = dlqMessageStore;
        this.flowDefinitionRepositoryPort = flowDefinitionRepositoryPort;
    }

    @Override
    public Health health() {
        int dlqCount = dlqMessageStore != null ? dlqMessageStore.getAll().size() : 0;
        int registeredFlows = flowDefinitionRepositoryPort != null ? flowDefinitionRepositoryPort.findAll().size() : 0;

        Map<String, Object> details = new HashMap<>();
        details.put("plataforma", "Root L Flux");
        details.put("versao", "0.1.0-SNAPSHOT");
        details.put("statusOperacional", "Operacional");
        details.put("mensagensDlqRetidas", dlqCount);
        details.put("fluxosRegistrados", registeredFlows);

        if (dlqCount > 100) {
            return Health.status("ALERTA")
                    .withDetails(details)
                    .withDetail("mensagem", "Volume elevado de mensagens retidas na Dead Letter Queue")
                    .build();
        }

        return Health.up().withDetails(details).build();
    }
}
