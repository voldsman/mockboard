package dev.mockboard.service;

import dev.mockboard.core.common.domain.dto.MockRuleDto;
import dev.mockboard.core.common.mapper.MockRuleMapper;
import dev.mockboard.core.engine.PathMatchingEngine;
import dev.mockboard.storage.cache.MockRuleCacheStore;
import dev.mockboard.storage.repo.BoardRepository;
import dev.mockboard.storage.repo.MockRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockExecutionService {

    // REWORK TO USE MEM CACHE INSTEAD
//    private final Cache<String, PathMatchingEngine> enginesByApiKey = Caffeine.newBuilder()
//            .maximumSize(10_000)
//            .expireAfterAccess(Duration.ofMinutes(30))
//            .build();
    private final Map<String, PathMatchingEngine> enginesByApi = new ConcurrentHashMap<>();
    private final MockRuleCacheStore mockRuleCacheStore;
    private final MockRuleRepository mockRuleRepository;
    private final MockRuleMapper mockRuleMapper;
    private final BoardRepository boardRepository;

    public Optional<MockRuleDto> findMatchingRule(String apiKey, String path, String method) {
        var engine = enginesByApi.computeIfAbsent(apiKey, this::buildEngine);
        var mockIdOpt = engine.match(path);
        if (mockIdOpt.isEmpty()) {
            log.debug("No path match found for apiKey={}, path={}", apiKey, path);
            return Optional.empty();
        }

        var mockId = mockIdOpt.get();
        log.trace("Path matched mockId={} for apiKey={}, path={}", mockId, apiKey, path);
        return getMockRule(apiKey, mockId, method);
    }

    public void invalidateEngine(String apiKey) {
        var removed = enginesByApi.remove(apiKey);
        if (removed != null) {
            log.info("Invalidated engine for apiKey={}, size was {}", apiKey, removed.size());
        }
    }

    public void invalidateAllEngines() {
        log.info("Invalidating all engines...");
        var enginesCount = enginesByApi.size();
        enginesByApi.clear();
        log.info("Invalidated {} engines.", enginesCount);
    }

    private PathMatchingEngine buildEngine(String apiKey) {
        log.debug("building engine for apiKey {}", apiKey);

        var engine = new PathMatchingEngine();
        var boardDocOpt = boardRepository.findByApiKey(apiKey);
        if (boardDocOpt.isEmpty()) {
            log.warn("no board found for apiKey={}", apiKey);
            return engine;
        }

        var boardDoc = boardDocOpt.get();
        var mockRules = mockRuleRepository.findByBoardId(boardDoc.getId());
        var mockDtos = new ArrayList<MockRuleDto>();

        int registered = 0;
        for (var mockRule : mockRules) {
            try {
                engine.register(mockRule.getPath(), mockRule.getId());
                mockDtos.add(mockRuleMapper.mapMockRuleDocToMockRuleDto(mockRule));
                registered++;
            } catch (Exception e) {
                log.error("Failed to register pattern [{}] for mock rule id={}, {}", mockRule.getPath(), mockRule.getId(), e.getMessage());
            }
        }

        mockDtos.forEach(dto -> mockRuleCacheStore.addMockRuleToCache(apiKey, dto));
        log.info("Build engine for apiKey={} with {}/{} patterns", apiKey, registered, mockRules.size());
        return engine;
    }

    private Optional<MockRuleDto> getMockRule(String apiKey, String mockId, String method) {
        var cachedMocks = mockRuleCacheStore.getMockRules(apiKey);
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

        log.debug("Cache miss for mockId={}, fallback to DB", mockId);
        var mockOpt = mockRuleRepository.findById(mockId);
        if (mockOpt.isEmpty()) {
            log.warn("Mock not found in DB: mockId={}", mockId);
            return Optional.empty();
        }

        var mock = mockOpt.get();
        if (!mock.getMethod().equalsIgnoreCase(method)) {
            log.debug("Method mismatch: excepted {}, got {}", mock.getMethod(), method);
            return Optional.empty();
        }

        return Optional.of(mockRuleMapper.mapMockRuleDocToMockRuleDto(mock));
    }
}
