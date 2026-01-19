package dev.mockboard.event;

import dev.mockboard.Constants;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class FifoBuffer<T> implements EventBuffer<T> {

    private final BlockingQueue<DomainEvent<T>> queue = new LinkedBlockingQueue<>();//Constants.EVENT_FIFO_QUEUE_CAPACITY

    @Override
    public void add(DomainEvent<T> event) {
        var added = queue.offer(event);
        log.debug("event={} for class={} added={}", event, event.getEntityClass().getSimpleName(), added);
    }

    @Override
    public List<DomainEvent<T>> drain(int maxElements) {
        var batch = new ArrayList<DomainEvent<T>>();
        queue.drainTo(batch, maxElements);
        return batch;
    }

    @Override
    public int size() {
        return queue.size();
    }
}
