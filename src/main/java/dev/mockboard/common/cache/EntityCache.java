package dev.mockboard.common.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.util.Optional;
import java.util.function.Function;

public interface EntityCache<T> {
    Optional<T> get(String key);
    Optional<T> get(String key, Function<String, T> loader);
    void put(String key, T value);
    void invalidate(String key);
    void invalidateAll();
    long size();
    CacheStats stats();
}
