package com.rootl.flux.infrastructure.adapter.out.messaging;

import com.rootl.flux.application.port.out.EventPublisherPort;
import com.rootl.flux.domain.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisherAdapter.class);

    @Override
    public void publish(Event event) {
        log.info("[PublicadorDeEventos] Evento publicado com sucesso: id={} tipo={} correlationId={}",
                event.getId().value(),
                event.getType(),
                event.getMetadata().correlationId());
    }
}
