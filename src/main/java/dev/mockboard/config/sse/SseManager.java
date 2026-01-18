package dev.mockboard.config.sse;

import dev.mockboard.Constants;
import dev.mockboard.common.domain.dto.BoardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseManager {

    private final Map<String, List<SseEmitter>> webhookEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(BoardDto boardDto) {
        var emitter = new SseEmitter(Constants.SSE_EMITTER_TTL);
        webhookEmitters.compute(boardDto.getId(), (key, emitters) -> {
            var newList = (CollectionUtils.isEmpty(emitters))
                    ? new CopyOnWriteArrayList<SseEmitter>()
                    : emitters;

            if (newList.size() >= Constants.MAX_SSE_EMITTERS_PER_BOARD) {
                try {
                    var oldest = newList.removeFirst();
                    oldest.complete();
                    log.debug("Emitters size exceeded, oldest removed");
                } catch (Exception e) {
                    // should be already dead
                    log.debug(e.getMessage(), e);
                }
            }

            newList.add(emitter);
            return newList;
        });

        emitter.onCompletion(cleanup(boardDto.getId(), emitter));
        emitter.onTimeout(cleanup(boardDto.getId(), emitter));
        emitter.onError(e -> cleanup(boardDto.getId(), emitter).run());

        try {
            emitter.send(SseEmitter.event()
                    .name(Constants.SSE_EMITTER_EVENT_PING)
                    .comment("established")
            );
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        return emitter;
    }

    private Runnable cleanup(String apiKey, SseEmitter emitter) {
        return () -> webhookEmitters.computeIfPresent(apiKey, (key, list) -> {
            list.remove(emitter);
            return list.isEmpty() ? null : list;
        });
    }

    public void broadcast(String apiKey, Object data) {
        var emitters = webhookEmitters.get(apiKey);
        if (CollectionUtils.isEmpty(emitters)) {
            return;
        }

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(Constants.SSE_EMITTER_EVENT_WEBHOOK)
                        .id(UUID.randomUUID().toString())
                        .data(data, MediaType.APPLICATION_JSON)
                );
            } catch (Exception e) {
                log.debug("An exception sending to disconnected emitter, {}", e.getMessage(), e);
                emitter.completeWithError(e);
            }
        });
    }

    @Scheduled(fixedRate = Constants.SSE_EMITTER_HEARTBEAT_RATE)
    public void sendHeartbeat() {
        if (webhookEmitters.isEmpty()) {
            log.debug("No emitters found, nothing to send");
            return;
        }

        log.trace("Sending heartbeat to {} active boards", webhookEmitters.size());
        webhookEmitters.forEach((key, emitters) -> emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(Constants.SSE_EMITTER_EVENT_PING)
                        .comment("heartbeat")
                );
            } catch (Exception e) {
                log.debug("An exception sending to disconnected emitter, {}", e.getMessage(), e);
                emitter.completeWithError(e);
                // probably not needed, spring should handle it
                // cleanup(key, emitter).run();
            }
        }));
    }

    public void onShutdown() {
        log.info("Shutting down SSE service: closing {} active boards", webhookEmitters.size());

        webhookEmitters.forEach((key, emitters) -> emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(Constants.SSE_EMITTER_EVENT_SHUTDOWN)
                        .reconnectTime(0)
                        .data("shutdown"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }));
        webhookEmitters.clear();
    }
}
