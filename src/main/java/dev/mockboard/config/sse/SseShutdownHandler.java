package dev.mockboard.config.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseShutdownHandler implements SmartLifecycle {

    private final SseManager sseManager;
    private boolean isRunning = false;

    @Override
    public void start() {
        this.isRunning = true;
    }

    @Override
    public void stop() {
        sseManager.onShutdown();
        this.isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE - 100;
    }
}
