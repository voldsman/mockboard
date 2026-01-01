package dev.mockboard.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import dev.mockboard.core.AppProperties;
import dev.mockboard.core.engine.PathMatchingEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockExecutionCacheStore extends BaseCacheStore<String, PathMatchingEngine> {

    // apiKey as key
    private final Cache<String, PathMatchingEngine> enginesByApiKey;

    public MockExecutionCacheStore(AppProperties appProperties) {
        this.enginesByApiKey = buildCache(appProperties);
    }

    public void addEngineCache(String apiKey, PathMatchingEngine engine) {
        log.debug("Adding engine cache for apiKey {}", apiKey);
        enginesByApiKey.put(apiKey, engine);
    }

    public PathMatchingEngine getEngineByApiKey(String apiKey) {
        log.debug("Get engine cache for apiKey {}", apiKey);
        return enginesByApiKey.getIfPresent(apiKey);
    }

    public void evict(String apiKey) {
        enginesByApiKey.invalidate(apiKey);
    }

    public void evictAll() {
        enginesByApiKey.invalidateAll();
    }
}
