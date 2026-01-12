package dev.mockboard.cache;

import dev.mockboard.cache.config.CaffeineEntityCache;
import dev.mockboard.common.engine.PathMatchingEngine;
import org.springframework.stereotype.Component;

import static dev.mockboard.Constants.DEFAULT_CACHE_EXP_AFTER_ACCESS_MINUTES;
import static dev.mockboard.Constants.DEFAULT_CACHE_MAX_ENTRIES;

@Component
public class MatchingEngineCache extends CaffeineEntityCache<PathMatchingEngine> {

    public MatchingEngineCache() {
        super(DEFAULT_CACHE_MAX_ENTRIES, DEFAULT_CACHE_EXP_AFTER_ACCESS_MINUTES);
    }

    public PathMatchingEngine getEngine(String key) {
        return cache.getIfPresent(key);
    }
}
