package dev.mockboard.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.mockboard.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class RateLimiterCache {

    // make the multi cache for now
    // later on figure out and rework to type based single cahce + time window
    private final Cache<String, AtomicInteger> boardCreationCache;
    private final Cache<String, AtomicInteger> mockExecutionCache;
    private final Cache<String, AtomicInteger> otherRequestsCache;

    public RateLimiterCache() {
        this.boardCreationCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();

        this.mockExecutionCache =  Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        this.otherRequestsCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    public boolean allowBoardCreation(String key) {
        return checkLimit(key, boardCreationCache, Constants.RATE_LIMIT_MAX_BOARDS_PER_HOUR);
    }

    public boolean allowMockExecution(String key) {
        return checkLimit(key, mockExecutionCache, Constants.RATE_LIMIT_MAX_MOCK_EXECUTIONS_PER_MINUTE);
    }

    public boolean allowOtherRequests(String key) {
        return checkLimit(key, otherRequestsCache, Constants.RATE_LIMIT_MAX_OTHER_REQUESTS_PER_MINUTE);
    }

    private boolean checkLimit( String key, Cache<String, AtomicInteger> cache, int limit) {
        var count = cache.get(key, k -> new AtomicInteger(0));
        return count.incrementAndGet() <= limit;
    }
}
