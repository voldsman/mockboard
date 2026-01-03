package dev.mockboard.service;

import dev.mockboard.core.common.domain.dto.BoardDto;
import dev.mockboard.core.common.domain.dto.MockRuleDto;
import dev.mockboard.core.common.domain.response.IdResponse;
import dev.mockboard.core.common.exception.NotFoundException;
import dev.mockboard.core.common.mapper.MockRuleMapper;
import dev.mockboard.core.common.validator.MockRuleValidator;
import dev.mockboard.storage.cache.MockExecutionCacheStore;
import dev.mockboard.storage.cache.MockRuleCacheStore;
import dev.mockboard.storage.doc.MockRuleDoc;
import dev.mockboard.storage.doc.repo.MockRuleRepository;
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

    private final BoardService boardService;
    private final MockRuleValidator mockRuleValidator;
    private final MockRuleRepository mockRuleRepository;
    private final MockRuleMapper mockRuleMapper;

    private final MockRuleCacheStore mockRuleCacheStore;
    private final MockExecutionCacheStore mockExecutionCacheStore;

    public IdResponse addMockRule(BoardDto boardDto, MockRuleDto mockRuleDto) {
        log.debug("addMockRule for boardId={}", boardDto.getId());
        mockRuleValidator.validateMockRule(mockRuleDto);

        // todo: validate mock exists by path

        mockRuleDto.setId(null);
        mockRuleDto.setBoardId(boardDto.getId());
        mockRuleDto.setApiKey(boardDto.getApiKey());
        mockRuleDto.setCreatedAt(LocalDateTime.now());
        var mockRuleDoc = mockRuleMapper.mapMockRuleDtoToMockRuleDoc(mockRuleDto);
        mockRuleRepository.save(mockRuleDoc);

        evictMockCache(boardDto.getApiKey());
        return new IdResponse(mockRuleDoc.getId());
    }

    public IdResponse updateMockRule(BoardDto boardDto, String mockRuleId, MockRuleDto mockRuleDto) {
        log.debug("updateMockRule boardId={} mockRuleId={}", boardDto.getId(), mockRuleId);
        mockRuleValidator.validateMockRule(mockRuleDto);

        var mockRule = getMockRuleDoc(mockRuleId, boardDto.getId());
        // map manually to avoid swallowing
        mockRule.setMethod(mockRuleDto.getMethod());
        mockRule.setPath(mockRuleDto.getPath());
        mockRule.setHeaders(mockRuleDto.getHeaders());
        mockRule.setBody(mockRuleDto.getBody());
        mockRule.setStatusCode(mockRuleDto.getStatusCode());
        mockRuleRepository.save(mockRule);

        evictMockCache(boardDto.getApiKey());
        return new IdResponse(mockRule.getId());
    }

    public List<MockRuleDto> getMockRuleDtos(BoardDto boardDto) {
        var cachedMockRules = mockRuleCacheStore.getMockRules(boardDto.getApiKey());
        if (CollectionUtils.isEmpty(cachedMockRules)) {
            var persistedMockRules = getMockRuleDocs(boardDto.getId());
            if (CollectionUtils.isEmpty(persistedMockRules)) {
                return List.of();
            }

            var mockRuleDtos = persistedMockRules.stream()
                    .map(mockRuleMapper::mapMockRuleDocToMockRuleDto)
                    .toList();
            mockRuleCacheStore.initMockRulesCache(boardDto.getApiKey(), mockRuleDtos);
            return mockRuleDtos;
        }
        return cachedMockRules;
    }

    public List<MockRuleDto> getMockRuleDtos(String apiKey) {
        var cachedMockRules = mockRuleCacheStore.getMockRules(apiKey);
        if (CollectionUtils.isEmpty(cachedMockRules)) {
            var persisted = getMockRuleDocsByApiKey(apiKey);
            if (CollectionUtils.isEmpty(persisted)) {
                return List.of();
            }

            var mockRuleDtos = persisted.stream()
                    .map(mockRuleMapper::mapMockRuleDocToMockRuleDto)
                    .toList();
            mockRuleCacheStore.initMockRulesCache(apiKey, mockRuleDtos);
            return mockRuleDtos;
        }
        return cachedMockRules;
    }

    public void deleteMockRule(BoardDto boardDto, String mockRuleId) {
        log.debug("delete mockRule with id={}, boardId={}", mockRuleId, boardDto.getId());
        var mockRule = getMockRuleDoc(mockRuleId, boardDto.getId());
        mockRuleRepository.delete(mockRule);

        evictMockCache(boardDto.getApiKey());
    }

    private MockRuleDoc getMockRuleDoc(String mockRuleId, String boardId) {
        var mockRuleDocOpt = mockRuleRepository.findByIdAndBoardId(mockRuleId, boardId);
        if (mockRuleDocOpt.isEmpty()) {
            throw new NotFoundException("mockRule with id: " + mockRuleId + " not found");
        }
        return mockRuleDocOpt.get();
    }

    private List<MockRuleDoc> getMockRuleDocs(String boardId) {
        return mockRuleRepository.findByBoardId(boardId);
    }

    private List<MockRuleDoc> getMockRuleDocsByApiKey(String apiKey) {
        return mockRuleRepository.findByApiKey(apiKey);
    }

    private void evictMockCache(String apiKey) {
        mockRuleCacheStore.evict(apiKey);
        mockExecutionCacheStore.evict(apiKey);
        log.debug("evict for apiKey={}", apiKey);
    }
}
