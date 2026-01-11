package dev.mockboard.service;

import dev.mockboard.cache.MatchingEngineCache;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.engine.PathMatchingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PathMatchingService {

    private final BoardService boardService;
    private final MockRuleService mockRuleService;
    private final MatchingEngineCache mockExecutionCacheStore;

    public Optional<String> getMatchingMockRuleId(String apiKey, String path) {
        var boardDto = boardService.getBoardDto(apiKey);

        var engine = mockExecutionCacheStore.getEngine(apiKey);
        if (engine == null) {
            engine = buildEngine(boardDto);
            mockExecutionCacheStore.put(apiKey, engine);
        }

        var mockRuleIdOpt = engine.match(path);
        if (mockRuleIdOpt.isEmpty()) {
            log.debug("No matching rule found for apiKey: {}, path: {}", apiKey, path);
            return Optional.empty();
        }
        log.trace("Path matched mockId={} for apiKey={}, path={}", mockRuleIdOpt.get(), apiKey, path);
        return mockRuleIdOpt;
    }

    private PathMatchingEngine buildEngine(BoardDto boardDto) {
        log.debug("building engine for apiKey {}", boardDto.getId());

        var engine = new PathMatchingEngine();
        var mockRules = mockRuleService.getMockRules(boardDto);
        int registered = 0;
        for (var mockRule : mockRules) {
            try {
                engine.register(mockRule.getPath(), mockRule.getId());
                registered++;
            } catch (Exception e) {
                log.error("Failed to register pattern [{}] for mock rule id={}, {}", mockRule.getPath(), mockRule.getId(), e.getMessage());
            }
        }

        log.info("Build engine for apiKey={} with {}/{} patterns", boardDto.getId(), registered, mockRules.size());
        return engine;
    }
}
