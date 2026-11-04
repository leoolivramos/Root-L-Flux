package com.rootl.flux.application.port.out;

import com.rootl.flux.domain.model.Event;

public interface EventPublisherPort {
    void publish(Event event);
}
