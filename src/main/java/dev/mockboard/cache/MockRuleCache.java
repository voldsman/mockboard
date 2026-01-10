package dev.mockboard.cache;

import dev.mockboard.Constants;
import dev.mockboard.cache.config.CaffeineEntityCache;
import dev.mockboard.common.domain.dto.MockRuleDto;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static dev.mockboard.Constants.DEFAULT_CACHE_EXP_AFTER_ACCESS_MINUTES;
import static dev.mockboard.Constants.DEFAULT_CACHE_MAX_ENTRIES;

@Component
public class MockRuleCache extends CaffeineEntityCache<List<MockRuleDto>> {

    public MockRuleCache() {
        super(DEFAULT_CACHE_MAX_ENTRIES, DEFAULT_CACHE_EXP_AFTER_ACCESS_MINUTES);
    }

    public void addMockRules(String key, List<MockRuleDto> mockRules) {
        var list = new ArrayList<MockRuleDto>(Constants.MAX_MOCK_RULES);
        list.addAll(mockRules);
        cache.put(key, list);
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

    public void updateMockRule(String key, MockRuleDto mockRule) {
        cache.asMap().compute(key, (k, mocks) -> {
            if (CollectionUtils.isEmpty(mocks)) {
                return new ArrayList<>(List.of(mockRule));
            }

            var newList = new ArrayList<MockRuleDto>(mocks);
            newList.removeIf(m -> m.getId().equals(mockRule.getId()));
            newList.add(mockRule);
            return newList;
        });
    }

    public List<MockRuleDto> getMockRules(String key) {
        var mockRules = cache.getIfPresent(key);
        if (CollectionUtils.isEmpty(mockRules)) {
            return Collections.emptyList();
        }
        return mockRules.stream()
                .sorted(Comparator.comparing(MockRuleDto::getTimestamp).reversed())
                .toList();
    }

    public void deleteMockRule(String key, String mockRuleId) {
        cache.asMap().computeIfPresent(key, (k, mocks) -> {
            var newList = new ArrayList<>(mocks);
            newList.removeIf(mockRule -> mockRule.getId().equals(mockRuleId));
            return newList;
        });
    }
}
