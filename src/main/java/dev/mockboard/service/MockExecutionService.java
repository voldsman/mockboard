package dev.mockboard.service;

import dev.mockboard.cache.MockRuleCache;
import dev.mockboard.common.domain.dto.MockRuleDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockExecutionService {

    private final PathMatchingService pathMatchingService;
    private final MockRuleCache mockRuleCache;

    public Optional<MockRuleDto> findMatchingRule(String apiKey, String path, String method) {
        var mockIdOpt = pathMatchingService.getMatchingMockRuleId(apiKey, path);
        if (mockIdOpt.isEmpty()) {
            log.debug("No mockId matching apiKey={} and path={} found", apiKey, path);
            return Optional.empty();
        }

        return getMockRule(apiKey, mockIdOpt.get(), method);
    }

    private Optional<MockRuleDto> getMockRule(String apiKey, String mockId, String method) {
        var cachedMocks = mockRuleCache.getMockRules(apiKey);
        if (!CollectionUtils.isEmpty(cachedMocks)) {
            var cached = cachedMocks.stream()
                    .filter(mock -> mock.getId().equals(mockId))
                    .filter(mock -> mock.getMethod().equalsIgnoreCase(method))
                    .findFirst();

            if (cached.isPresent()) {
                log.trace("Cache hit for mockId={}, method={}", mockId, method);
                return cached;
            }
        }
        return Optional.empty();
    }
}
