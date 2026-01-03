package dev.mockboard.storage.cache.ratelimiter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.mockboard.core.AppProperties;
import dev.mockboard.core.common.exception.RateLimitExceededException;
import dev.mockboard.storage.cache.BaseCacheStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class MockExecutionRateLimiter extends BaseCacheStore<String, AtomicInteger> {

    private static final int MAX_REQUESTS_PER_MINUTE_PER_APIKEY = 20;

    private final Cache<String, AtomicInteger> requestCounts;

    public MockExecutionRateLimiter(AppProperties appProperties) {
        requestCounts = buildCache(appProperties.getMaxCacheEntries(), appProperties.getCacheMockExecutionExpireAfterAccessMinutes());
    }

    public void checkLimit(String apiKey) {
        var count = requestCounts.get(apiKey, k -> new AtomicInteger(0));
        assert count != null;
        if (count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE_PER_APIKEY) {
            throw new RateLimitExceededException("Too many requests.. chill");
        }
    }

    @Override
    public void evict(String apiKey) {
        requestCounts.invalidate(apiKey);
        log.debug("Evicting api key {}", apiKey);
    }

    @Override
    public void evictAll() {
        requestCounts.invalidateAll();
        log.debug("Evicting all api keys");
    }
}
