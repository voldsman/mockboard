package dev.mockboard.service;

import dev.mockboard.cache.MatchingEngineCache;
import dev.mockboard.cache.MockRuleCache;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.domain.dto.MockRuleDto;
import dev.mockboard.common.domain.response.IdResponse;
import dev.mockboard.common.utils.IdGenerator;
import dev.mockboard.common.utils.JsonUtils;
import dev.mockboard.common.validator.MockRuleValidator;
import dev.mockboard.event.config.DomainEvent;
import dev.mockboard.event.config.EventQueue;
import dev.mockboard.repository.MockRuleRepository;
import dev.mockboard.repository.model.MockRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockRuleService {

    private final EventQueue eventQueue;
    private final ModelMapper modelMapper;
    private final MockRuleValidator mockRuleValidator;
    private final MockRuleRepository mockRuleRepository;
    private final MockRuleCache mockRuleCache;
    private final MatchingEngineCache matchingEngineCache;

    public IdResponse createMockRule(BoardDto boardDto, MockRuleDto mockRuleDto) {
        log.debug("creating mock rule for boardId={}", boardDto.getId());
        mockRuleValidator.validateMockRule(mockRuleDto);

        mockRuleDto.setId(IdGenerator.generateId());
        mockRuleDto.setBoardId(boardDto.getId());
        mockRuleDto.setApiKey(boardDto.getApiKey());
        mockRuleDto.setHeaders(JsonUtils.minify(mockRuleDto.getHeaders()));
        mockRuleDto.setBody(JsonUtils.minify(mockRuleDto.getBody()));
        mockRuleDto.setTimestamp(Instant.now());

        var mockRule = modelMapper.map(mockRuleDto, MockRule.class);
        mockRuleCache.addMockRule(boardDto.getApiKey(), mockRuleDto);

        eventQueue.publish(DomainEvent.create(mockRule, MockRule.class));
        log.info("Mock rule added bo board: {}", boardDto.getId());
        return new IdResponse(mockRule.getId());
    }

    public List<MockRuleDto> getMockRules(BoardDto boardDto) {
        var cachedMockRules = mockRuleCache.getMockRules(boardDto.getApiKey());
        if (CollectionUtils.isEmpty(cachedMockRules)) {
            var persistedMockRules = mockRuleRepository.findByBoardId(boardDto.getId());
            if (CollectionUtils.isEmpty(persistedMockRules)) {
                return Collections.emptyList();
            }

            var dtos = persistedMockRules.stream()
                    .map(mockRule -> modelMapper.map(mockRule, MockRuleDto.class))
                    .toList();
            mockRuleCache.addMockRules(boardDto.getApiKey(), dtos);
            return dtos;
        }
        return cachedMockRules;
    }

    public void deleteMockRule(BoardDto boardDto, String mockRuleId) {
        log.info("deleting mock rule={} for boardId={}", mockRuleId, boardDto.getId());
        mockRuleCache.deleteMockRule(boardDto.getApiKey(), mockRuleId);

        // hard delete. maybe migrate to soft delete later
        eventQueue.publish(DomainEvent.delete(mockRuleId, MockRule.class));
    }

    //    public List<MockRuleDto> getMockRuleDtos(BoardDto boardDto) {
//        var cachedMockRules = mockRuleCacheStore.getMockRules(boardDto.getApiKey());
//        if (CollectionUtils.isEmpty(cachedMockRules)) {
//            var persistedMockRules = getMockRuleDocs(boardDto.getId());
//            if (CollectionUtils.isEmpty(persistedMockRules)) {
//                return List.of();
//            }
//
//            var mockRuleDtos = persistedMockRules.stream()
//                    .map(mockRuleMapper::mapMockRuleDocToMockRuleDto)
//                    .toList();
//            mockRuleCacheStore.initMockRulesCache(boardDto.getApiKey(), mockRuleDtos);
//            return mockRuleDtos;
//        }
//        return cachedMockRules;
//    }
//    public IdResponse updateMockRule(BoardDto boardDto, String mockRuleId, MockRuleDto mockRuleDto) {
//        log.debug("updateMockRule boardId={} mockRuleId={}", boardDto.getId(), mockRuleId);
//        mockRuleValidator.validateMockRule(mockRuleDto);
//
//        var mockRule = getMockRuleDoc(mockRuleId, boardDto.getId());
//        // map manually to avoid swallowing
//        mockRule.setMethod(mockRuleDto.getMethod());
//        mockRule.setPath(mockRuleDto.getPath());
//        mockRule.setHeaders(mockRuleDto.getHeaders());
//        mockRule.setBody(mockRuleDto.getBody());
//        mockRule.setStatusCode(mockRuleDto.getStatusCode());
//        mockRuleRepository.save(mockRule);
//
//        evictMockCache(boardDto.getApiKey());
//        return new IdResponse(mockRule.getId());
//    }
//

//
//    public List<MockRuleDto> getMockRuleDtos(String apiKey) {
//        var cachedMockRules = mockRuleCacheStore.getMockRules(apiKey);
//        if (CollectionUtils.isEmpty(cachedMockRules)) {
//            var persisted = getMockRuleDocsByApiKey(apiKey);
//            if (CollectionUtils.isEmpty(persisted)) {
//                return List.of();
//            }
//
//            var mockRuleDtos = persisted.stream()
//                    .map(mockRuleMapper::mapMockRuleDocToMockRuleDto)
//                    .toList();
//            mockRuleCacheStore.initMockRulesCache(apiKey, mockRuleDtos);
//            return mockRuleDtos;
//        }
//        return cachedMockRules;
//    }
//
//    public void deleteMockRule(BoardDto boardDto, String mockRuleId) {
//        log.debug("delete mockRule with id={}, boardId={}", mockRuleId, boardDto.getId());
//        var mockRule = getMockRuleDoc(mockRuleId, boardDto.getId());
//        mockRuleRepository.delete(mockRule);
//
//        evictMockCache(boardDto.getApiKey());
//    }
//
//    private MockRuleDoc getMockRuleDoc(String mockRuleId, String boardId) {
//        var mockRuleDocOpt = mockRuleRepository.findByIdAndBoardId(mockRuleId, boardId);
//        if (mockRuleDocOpt.isEmpty()) {
//            throw new NotFoundException("mockRule with id: " + mockRuleId + " not found");
//        }
//        return mockRuleDocOpt.get();
//    }
//
//    private List<MockRuleDoc> getMockRuleDocs(String boardId) {
//        return mockRuleRepository.findByBoardId(boardId);
//    }
//
//    private List<MockRuleDoc> getMockRuleDocsByApiKey(String apiKey) {
//        return mockRuleRepository.findByApiKey(apiKey);
//    }
//
//    private void evictMockCache(String apiKey) {
//        mockRuleCacheStore.evict(apiKey);
//        mockExecutionCacheStore.evict(apiKey);
//        log.debug("evict for apiKey={}", apiKey);
//    }
}
