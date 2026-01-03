package dev.mockboard.service;

import dev.mockboard.core.engine.PathMatchingEngine;
import dev.mockboard.storage.cache.MockExecutionCacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathMatchingService {

    private final MockRuleService mockRuleService;
    private final MockExecutionCacheStore mockExecutionCacheStore;

    public Optional<String> getMatchingMockRuleId(String apiKey, String path) {
        var engine = mockExecutionCacheStore.getEngineByApiKey(apiKey);
        if (engine == null) {
            engine = buildEngine(apiKey);
            mockExecutionCacheStore.addEngineCache(apiKey, engine);
        }

        var mockRuleIdOpt = engine.match(path);
        if (mockRuleIdOpt.isEmpty()) {
            log.debug("No matching rule found for apiKey: {}, path: {}", apiKey, path);
            return Optional.empty();
        }
        log.trace("Path matched mockId={} for apiKey={}, path={}", mockRuleIdOpt.get(), apiKey, path);
        return mockRuleIdOpt;
    }

    private PathMatchingEngine buildEngine(String apiKey) {
        log.debug("building engine for apiKey {}", apiKey);

        var engine = new PathMatchingEngine();
        var mockRules = mockRuleService.getMockRuleDtos(apiKey);
        int registered = 0;
        for (var mockRule : mockRules) {
            try {
                engine.register(mockRule.getPath(), mockRule.getId());
                registered++;
            } catch (Exception e) {
                log.error("Failed to register pattern [{}] for mock rule id={}, {}", mockRule.getPath(), mockRule.getId(), e.getMessage());
            }
        }

        log.info("Build engine for apiKey={} with {}/{} patterns", apiKey, registered, mockRules.size());
        return engine;
    }
}
