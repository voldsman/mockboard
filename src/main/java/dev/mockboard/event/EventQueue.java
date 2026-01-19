package dev.mockboard.event;

import dev.mockboard.repository.model.Board;
import dev.mockboard.repository.model.MockRule;
import dev.mockboard.repository.model.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EventQueue {

    private final Map<Class<?>, EventBuffer<?>> buffers = new ConcurrentHashMap<>();

    public EventQueue() {
        buffers.put(Board.class, new FifoBuffer<>());
        buffers.put(MockRule.class, new FifoBuffer<>());

        // webhooks should handle duplications
        buffers.put(Webhook.class, new DedupBuffer<>());
        log.info("Initialized event queue with {} buffers", buffers.size());
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(DomainEvent<T> event) {
        var buffer = (EventBuffer<T>) buffers.get(event.getEntityClass());
        if (buffer != null) {
            buffer.add(event);
        } else log.error("unknown event type: {}", event.getType());
    }

    @SuppressWarnings("unchecked")
    public <T> List<DomainEvent<T>> drain(Class<T> clazz, int maxElements) {
        var buffer = (EventBuffer<T>) buffers.get(clazz);
        if (buffer != null) {
            return buffer.drain(maxElements);
        }
        return Collections.emptyList();
    }
}
