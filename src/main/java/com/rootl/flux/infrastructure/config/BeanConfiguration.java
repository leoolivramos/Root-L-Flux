package com.rootl.flux.infrastructure.config;

import com.rootl.flux.application.port.in.ExecuteFlowUseCase;
import com.rootl.flux.application.port.in.IngestEventUseCase;
import com.rootl.flux.application.port.in.ManageFlowDefinitionUseCase;
import com.rootl.flux.application.port.in.QueryFlowExecutionUseCase;
import com.rootl.flux.application.port.in.ReplayExecutionUseCase;
import com.rootl.flux.application.port.out.*;
import com.rootl.flux.application.service.EventIngestionService;
import com.rootl.flux.application.service.FlowDefinitionService;
import com.rootl.flux.application.service.FlowExecutionOrchestrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public ManageFlowDefinitionUseCase manageFlowDefinitionUseCase(FlowDefinitionRepositoryPort repository) {
        return new FlowDefinitionService(repository);
    }

    @Bean
    public FlowExecutionOrchestrator flowExecutionOrchestrator(
            FlowExecutionRepositoryPort flowExecutionRepositoryPort,
            FlowDefinitionRepositoryPort flowDefinitionRepositoryPort,
            StepExecutionRepositoryPort stepExecutionRepositoryPort,
            EventRepositoryPort eventRepositoryPort,
            MetricsPort metricsPort) {
        return new FlowExecutionOrchestrator(
                flowExecutionRepositoryPort,
                flowDefinitionRepositoryPort,
                stepExecutionRepositoryPort,
                eventRepositoryPort,
                metricsPort
        );
    }

    @Bean
    public ExecuteFlowUseCase executeFlowUseCase(FlowExecutionOrchestrator orchestrator) {
        return orchestrator;
    }

    @Bean
    public QueryFlowExecutionUseCase queryFlowExecutionUseCase(FlowExecutionOrchestrator orchestrator) {
        return orchestrator;
    }

    @Bean
    public ReplayExecutionUseCase replayExecutionUseCase(FlowExecutionOrchestrator orchestrator) {
        return orchestrator;
    }

    @Bean
    public IngestEventUseCase ingestEventUseCase(
            EventRepositoryPort eventRepositoryPort,
            EventPublisherPort eventPublisherPort,
            FlowDefinitionRepositoryPort flowDefinitionRepositoryPort,
            ExecuteFlowUseCase executeFlowUseCase,
            MetricsPort metricsPort) {
        return new EventIngestionService(
                eventRepositoryPort,
                eventPublisherPort,
                flowDefinitionRepositoryPort,
                executeFlowUseCase,
                metricsPort
        );
    }
}
