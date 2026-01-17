package dev.mockboard.event;

import dev.mockboard.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class EventQueue {

    private final Map<EventType, Map<Class<?>, BlockingQueue<DomainEvent<?>>>> queues = new ConcurrentHashMap<>();

    public EventQueue() {
        for (EventType eventType : EventType.values()) {
            queues.put(eventType, new ConcurrentHashMap<>());
        }
    }

    public <T> void publish(DomainEvent<T> event) {
        var typeQueues = queues.get(event.getType());
        var queue = typeQueues.computeIfAbsent(event.getEntityClass(),
                k -> new LinkedBlockingQueue<>(Constants.EVENT_QUEUE_CAPACITY));
        var added = queue.offer(event);
        log.debug("Event {} added to queue, result={}", event.getType(), added);
    }

    @SuppressWarnings("unchecked")
    public <T> List<DomainEvent<T>> drain(EventType type, Class<T> entityClass, int maxElements) {
        var typeQueue = queues.get(type);
        var queue = typeQueue.get(entityClass);
        if (queue == null) {
            return List.of();
        }

        var events = new ArrayList<DomainEvent<?>>(maxElements);
        queue.drainTo(events, maxElements);
        return events.stream()
                .map(event -> (DomainEvent<T>) event)
                .toList();
    }

    public int getQueueSize(EventType type) {
        return queues.get(type).size();
    }
}
