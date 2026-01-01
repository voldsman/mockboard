package dev.mockboard.service;

import dev.mockboard.core.common.doc.MockRuleDoc;
import dev.mockboard.core.common.domain.dto.MockRuleDto;
import dev.mockboard.core.common.domain.response.IdResponse;
import dev.mockboard.core.common.exception.NotFoundException;
import dev.mockboard.core.common.mapper.MockRuleMapper;
import dev.mockboard.core.common.validator.MockRuleValidator;
import dev.mockboard.storage.cache.BoardCacheStore;
import dev.mockboard.storage.cache.MockExecutionCacheStore;
import dev.mockboard.storage.cache.MockRuleCacheStore;
import dev.mockboard.storage.repo.MockRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockRuleService {

    private final MockRuleValidator mockRuleValidator;
    private final MockRuleRepository mockRuleRepository;
    private final MockRuleMapper mockRuleMapper;

    private final MockRuleCacheStore mockRuleCacheStore;
    private final MockExecutionCacheStore mockExecutionCacheStore;
    private final BoardCacheStore boardCacheStore;

    public IdResponse addMockRule(String boardId, MockRuleDto mockRuleDto) {
        log.debug("addMockRule for boardId={}", boardId);
        mockRuleValidator.validateMockRule(mockRuleDto);

        // todo: validate mock exists by path

        mockRuleDto.setId(null);
        mockRuleDto.setBoardId(boardId);
        mockRuleDto.setCreatedAt(LocalDateTime.now());
        var mockRuleDoc = mockRuleMapper.mapMockRuleDtoToMockRuleDoc(mockRuleDto);
        mockRuleRepository.save(mockRuleDoc);

        evictMockExecutionCache(boardId);
        return new IdResponse(mockRuleDoc.getId());
    }

    public IdResponse updateMockRule(String boardId, String mockRuleId, MockRuleDto mockRuleDto) {
        log.debug("updateMockRule boardId={} mockRuleId={}", boardId, mockRuleId);
        mockRuleValidator.validateMockRule(mockRuleDto);

        var mockRule = getMockRuleDoc(mockRuleId, boardId);
        // map manually to avoid swallowing
        mockRule.setMethod(mockRuleDto.getMethod());
        mockRule.setPath(mockRuleDto.getPath());
        mockRule.setHeaders(mockRuleDto.getHeaders());
        mockRule.setBody(mockRuleDto.getBody());
        mockRule.setStatusCode(mockRuleDto.getStatusCode());
        mockRuleRepository.save(mockRule);

        evictMockExecutionCache(boardId);
        return new IdResponse(mockRule.getId());
    }

    public List<MockRuleDto> getMockRules(String boardId) {
        return mockRuleRepository.findByBoardId(boardId)
                .stream()
                .map(mockRuleMapper::mapMockRuleDocToMockRuleDto)
                .toList();
    }

    public List<MockRuleDto> getMockRulesCached(String boardId, String apiKey) {
        var cachedMockRules = mockRuleCacheStore.getMockRules(boardId);
        if (CollectionUtils.isEmpty(cachedMockRules)) {
            var persistedMockRuleDtos = getMockRules(boardId);
            if (CollectionUtils.isEmpty(persistedMockRuleDtos)) {
                return List.of();
            }

            mockRuleCacheStore.initMockRulesCache(apiKey, persistedMockRuleDtos);
            return persistedMockRuleDtos;
        }

        return cachedMockRules;
    }

    public void deleteMockRule(String mockRuleId, String boardId) {
        log.debug("delete mockRule with id={}, boardId={}", mockRuleId, boardId);
        var mockRule = getMockRuleDoc(mockRuleId, boardId);
        mockRuleRepository.delete(mockRule);

        evictMockExecutionCache(boardId);
    }

    private MockRuleDoc getMockRuleDoc(String mockRuleId, String boardId) {
        var mockRuleDocOpt = mockRuleRepository.findByIdAndBoardId(mockRuleId, boardId);
        if (mockRuleDocOpt.isEmpty()) {
            throw new NotFoundException("mockRule with id: " + mockRuleId + " not found");
        }
        return mockRuleDocOpt.get();
    }

    private void evictMockExecutionCache(String boardId) {
        var board = boardCacheStore.getBoardCache(boardId);
        if (board == null) return;
        mockExecutionCacheStore.evict(board.getApiKey());
    }
}
