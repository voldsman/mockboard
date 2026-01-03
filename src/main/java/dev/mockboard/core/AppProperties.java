package dev.mockboard.core;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppProperties {

    @Value("${mockboard.security.owner-token-header-key}")
    private String ownerTokenHeader;

    // cache props
    @Value("${mockboard.cache.max-entries}")
    private int maxCacheEntries;

    @Value("${mockboard.cache.exp-after-access-minutes}")
    private int cacheExpireAfterAccessMinutes;

    @Value("${mockboard.cache.rate-limiter.board-creation-exp-after-access-minutes}")
    private int cacheBoardCreationExpireAfterAccessMinutes;

    @Value("${mockboard.cache.rate-limiter.mock-execution-exp-after-access-minutes}")
    private int cacheMockExecutionExpireAfterAccessMinutes;
}
