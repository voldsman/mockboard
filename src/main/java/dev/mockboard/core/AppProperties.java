package dev.mockboard.core;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AppProperties {

    @Value("${mockboard.security.owner-token-header-key}")
    private String ownerTokenHeader;

    @Value("${mockboard.cache.max-entries}")
    private int maxCacheEntries;

    @Value("${mockboard.cache.expire-after-access-minutes}")
    private int cacheExpireAfterAccessMinutes;
}
