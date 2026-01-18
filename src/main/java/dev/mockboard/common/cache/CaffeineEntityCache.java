package dev.mockboard.common.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public abstract class CaffeineEntityCache<T> implements EntityCache<T> {

    protected final Cache<String, T> cache;

    public CaffeineEntityCache(long maxSize, int ttlMinutes) {
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterAccess(ttlMinutes, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    @Override
    public Optional<T> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    @Override
    public Optional<T> get(String key, Function<String, T> loader) {
        return Optional.ofNullable(cache.get(key, loader));
    }

    @Override
    public void put(String key, T value) {
        cache.put(key, value);
    }

    @Override
    public void invalidate(String key) {
        cache.invalidate(key);
    }

    @Override
    public void invalidateAll() {
        cache.invalidateAll();
    }

    @Override
    public long size() {
        return cache.estimatedSize();
    }

    @Override
    public CacheStats stats() {
        return cache.stats();
    }
}
