package dev.mockboard.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import dev.mockboard.core.AppProperties;
import dev.mockboard.core.common.domain.dto.MockRuleDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class MockRuleCacheStore extends BaseCacheStore<String, List<MockRuleDto>> {

    // apiKey as key
    private final Cache<String, List<MockRuleDto>> mockRules;

    public MockRuleCacheStore(AppProperties appProperties) {
        this.mockRules = buildCache(appProperties);
    }

    public void initMockRulesCache(String apiKey, List<MockRuleDto> mockRuleDtos) {
        log.debug("init mock rules cache for apiKey={}", apiKey);
        mockRules.put(apiKey, mockRuleDtos);
    }

    public void addMockRuleToCache(String apiKey, MockRuleDto mockRule) {
        var cachedRules = getMockRules(apiKey);
        if (CollectionUtils.isEmpty(cachedRules)) {
            var mockRulesToCache = new ArrayList<MockRuleDto>();
            mockRulesToCache.add(mockRule);
            mockRules.put(apiKey, mockRulesToCache);
        } else {
            cachedRules.add(mockRule);
            mockRules.put(apiKey, cachedRules);
        }
    }

    public List<MockRuleDto> getMockRules(String apiKey) {
        return mockRules.getIfPresent(apiKey);
    }

    @Override
    public void evict(String apiKey) {
        log.debug("evict mock rules for apiKey={}", apiKey);
        mockRules.invalidate(apiKey);
    }

    public void evictAll() {
        log.info("Evict all mock rule entries");
        mockRules.invalidateAll();
    }
}
