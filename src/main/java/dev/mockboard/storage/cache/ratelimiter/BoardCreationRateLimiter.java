package dev.mockboard.storage.cache.ratelimiter;

import com.github.benmanes.caffeine.cache.Cache;
import dev.mockboard.core.AppProperties;
import dev.mockboard.core.common.exception.RateLimitExceededException;
import dev.mockboard.storage.cache.BaseCacheStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class BoardCreationRateLimiter extends BaseCacheStore<String, AtomicInteger> {

    private static final int MAX_BOARDS_PER_HOUR_PER_IP = 5;

    private final Cache<String, AtomicInteger> creationCounts;

    public BoardCreationRateLimiter(AppProperties appProperties) {
        this.creationCounts = buildCache(appProperties.getMaxCacheEntries(), appProperties.getCacheBoardCreationExpireAfterAccessMinutes());
    }

    public void checkLimit(String ip) {
        var count = creationCounts.get(ip, k -> new AtomicInteger(0));
        assert count != null;
        if (count.incrementAndGet() > MAX_BOARDS_PER_HOUR_PER_IP) {
            throw new RateLimitExceededException("Too many boards.. chill");
        }
    }

    @Override
    public void evict(String ip) {
        creationCounts.invalidate(ip);
        log.debug("evicting board {}", ip);
    }

    @Override
    public void evictAll() {
        creationCounts.invalidateAll();
        log.debug("evicting all boards");
    }
}
