package dev.mockboard.event;

import dev.mockboard.event.config.DomainEvent;
import dev.mockboard.event.config.EventQueue;
import dev.mockboard.event.config.EventType;
import dev.mockboard.repository.BoardRepository;
import dev.mockboard.repository.MockRuleRepository;
import dev.mockboard.repository.model.Board;
import dev.mockboard.repository.model.MockRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Objects;

import static dev.mockboard.Constants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EventQueue eventQueue;
    private final BoardRepository boardRepository;
    private final MockRuleRepository mockRuleRepository;

    @Scheduled(fixedDelay = CREATED_EVENTS_PROCESS_DELAY)
    public void processCreateEvents() {
        processBoards(EventType.CREATE);
        processMockRules(EventType.CREATE);
    }

    @Scheduled(fixedDelay = UPDATED_EVENTS_PROCESS_DELAY)
    public void processUpdateEvents() {
        processBoards(EventType.UPDATE);
        processMockRules(EventType.UPDATE);
    }

    @Scheduled(fixedDelay = DELETED_EVENTS_PROCESS_DELAY)
    public void processDeleteEvents() {
        processBoards(EventType.DELETE);
        processMockRules(EventType.DELETE);
    }

    private void processBoards(EventType type) {
        var events = eventQueue.drain(type, Board.class, MAX_EVENT_CONSUMER_DRAIN_ELEMS);
        if (CollectionUtils.isEmpty(events)) {
//            log.debug("No events found for type {}", type);
            return;
        }

        try {
            switch (type) {
                case CREATE -> {
                    var boards = events.stream()
                            .map(DomainEvent::getEntity)
                            .toList();
                    boardRepository.batchInsert(boards);
                    log.debug("Created {} boards in DB", boards.size());
                }
                case UPDATE -> {
                    log.warn("Batch board updates not yet implemented");
                }
                case DELETE -> {
                    var boardIds = events.stream()
                            .map(DomainEvent::getEntityId)
                            .filter(Objects::nonNull)
                            .toList();
                    boardRepository.batchDelete(boardIds);
                    log.debug("Deleted {} boards from DB", boardIds.size());
                }
            }
        } catch (Exception e) {
            log.error("Failed to process {} board events", type, e);
        }
    }

    private void processMockRules(EventType type) {
        var events = eventQueue.drain(type, MockRule.class, MAX_EVENT_CONSUMER_DRAIN_ELEMS);
        if (CollectionUtils.isEmpty(events)) {
//            log.debug("No events found for type {}", type);
            return;
        }

        try {
            switch (type) {
                case CREATE -> {
                    var mockRules = events.stream()
                            .map(DomainEvent::getEntity)
                            .toList();
                    mockRuleRepository.batchInsert(mockRules);
                    log.debug("Created {} mock rules in DB", mockRules.size());
                }
                case UPDATE -> {
                    var mockRules = events.stream()
                            .map(DomainEvent::getEntity)
                            .toList();
                    mockRuleRepository.batchUpdate(mockRules);
                    log.debug("Updated {} mock rules in DB", mockRules.size());
                }
                case DELETE -> {
                    var mockRuleIds = events.stream()
                            .map(DomainEvent::getEntityId)
                            .filter(Objects::nonNull)
                            .toList();
                    mockRuleRepository.batchDelete(mockRuleIds);
                    log.debug("Deleted {} mock rules from DB", mockRuleIds.size());
                }
            }
        } catch (Exception e) {
            log.error("Failed to process {} mock rules events", type, e);
        }
    }
}
