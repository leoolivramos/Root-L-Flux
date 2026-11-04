package com.rootl.flux.infrastructure.config;

import com.rootl.flux.application.port.in.ExecuteFlowUseCase;
import com.rootl.flux.application.port.in.IngestEventUseCase;
import com.rootl.flux.application.port.in.QueryFlowExecutionUseCase;
import com.rootl.flux.application.port.out.EventPublisherPort;
import com.rootl.flux.application.port.out.EventRepositoryPort;
import com.rootl.flux.application.port.out.FlowExecutionRepositoryPort;
import com.rootl.flux.application.service.EventIngestionService;
import com.rootl.flux.application.service.FlowExecutionOrchestrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public IngestEventUseCase ingestEventUseCase(EventRepositoryPort eventRepositoryPort,
                                                EventPublisherPort eventPublisherPort) {
        return new EventIngestionService(eventRepositoryPort, eventPublisherPort);
    }

    @Bean
    public FlowExecutionOrchestrator flowExecutionOrchestrator(
            FlowExecutionRepositoryPort flowExecutionRepositoryPort,
            EventRepositoryPort eventRepositoryPort) {
        return new FlowExecutionOrchestrator(flowExecutionRepositoryPort, eventRepositoryPort);
    }

    @Bean
    public ExecuteFlowUseCase executeFlowUseCase(FlowExecutionOrchestrator orchestrator) {
        return orchestrator;
    }

    @Bean
    public QueryFlowExecutionUseCase queryFlowExecutionUseCase(FlowExecutionOrchestrator orchestrator) {
        return orchestrator;
    }
}
