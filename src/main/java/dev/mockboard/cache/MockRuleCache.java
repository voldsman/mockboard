package dev.mockboard.cache;

import dev.mockboard.cache.config.CaffeineEntityCache;
import dev.mockboard.common.domain.dto.MockRuleDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static dev.mockboard.Constants.DEFAULT_CACHE_EXP_AFTER_ACCESS_MINUTES;
import static dev.mockboard.Constants.DEFAULT_CACHE_MAX_ENTRIES;

@Component
public class MockRuleCache extends CaffeineEntityCache<List<MockRuleDto>> {

    public MockRuleCache() {
        super(DEFAULT_CACHE_MAX_ENTRIES, DEFAULT_CACHE_EXP_AFTER_ACCESS_MINUTES);
    }

    public void addMockRule(String key, MockRuleDto mockRule) {
        cache.asMap().compute(key, (k, mocks) -> {
            var mutableMocks = (mocks == null)
                    ? new ArrayList<MockRuleDto>()
                    : new ArrayList<>(mocks);
            mutableMocks.add(mockRule);
            return mutableMocks;
        });
    }

    public List<MockRuleDto> getMockRules(String key) {
        return cache.getIfPresent(key);
    }
}
