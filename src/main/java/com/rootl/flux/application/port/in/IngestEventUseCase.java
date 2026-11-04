package com.rootl.flux.application.port.in;

import com.rootl.flux.domain.model.Event;

public interface IngestEventUseCase {

    Event ingest(IngestEventCommand command);

    record IngestEventCommand(
            String type,
            String source,
            String payload,
            String idempotencyKey,
            String correlationId,
            String traceparent
    ) {}
}
