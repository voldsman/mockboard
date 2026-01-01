package dev.mockboard.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.mockboard.core.AppProperties;

import java.time.Duration;

public abstract class BaseCacheStore<K, V> {

    public Cache<K, V> buildCache(AppProperties appProperties) {
        return Caffeine.newBuilder()
                .maximumSize(appProperties.getMaxCacheEntries())
                .expireAfterAccess(Duration.ofMinutes(appProperties.getCacheExpireAfterAccessMinutes()))
                .build();
    }

    public abstract void evict(String key);
    public abstract void evictAll();
}
