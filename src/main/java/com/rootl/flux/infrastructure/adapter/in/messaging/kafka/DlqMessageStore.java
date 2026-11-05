package com.rootl.flux.infrastructure.adapter.in.messaging.kafka;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class DlqMessageStore {

    public record DlqItem(
            String id,
            String key,
            String payload,
            String originalTopic,
            String exceptionMessage,
            Instant receivedAt
    ) {}

    private final List<DlqItem> items = Collections.synchronizedList(new ArrayList<>());

    public void add(DlqItem item) {
        items.add(item);
    }

    public List<DlqItem> getAll() {
        return List.copyOf(items);
    }

    public void clear() {
        items.clear();
    }

    public int size() {
        return items.size();
    }
}
