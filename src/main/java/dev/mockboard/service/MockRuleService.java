package dev.mockboard.service;

import dev.mockboard.Constants;
import dev.mockboard.cache.MatchingEngineCache;
import dev.mockboard.cache.MockRuleCache;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.domain.dto.MockRuleDto;
import dev.mockboard.common.domain.response.IdResponse;
import dev.mockboard.common.exception.BadRequestException;
import dev.mockboard.common.exception.NotFoundException;
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
        var existingMockRules = getMockRules(boardDto);
        if (existingMockRules.size() >= Constants.MAX_MOCK_RULES) {
            throw new BadRequestException("Maximum number of mock rules exceeded. Allowed: " + Constants.MAX_MOCK_RULES);
        }

        log.debug("creating mock rule for boardId={}", boardDto.getId());
        mockRuleValidator.validateMockRule(mockRuleDto);

        mockRuleDto.setId(IdGenerator.generateId());
        mockRuleDto.setBoardId(boardDto.getId());
        mockRuleDto.setHeaders(JsonUtils.minify(mockRuleDto.getHeaders()));
        mockRuleDto.setBody(JsonUtils.minify(mockRuleDto.getBody()));
        mockRuleDto.setTimestamp(Instant.now());

        var mockRule = modelMapper.map(mockRuleDto, MockRule.class);
        mockRuleCache.addMockRule(boardDto.getId(), mockRuleDto);
        matchingEngineCache.invalidate(boardDto.getId());

        eventQueue.publish(DomainEvent.create(mockRule, MockRule.class));
        log.info("Mock rule added bo board: {}", boardDto.getId());
        return new IdResponse(mockRule.getId());
    }

    public List<MockRuleDto> getMockRules(BoardDto boardDto) {
        var cachedMockRules = mockRuleCache.getMockRules(boardDto.getId());
        if (CollectionUtils.isEmpty(cachedMockRules)) {
            var persistedMockRules = mockRuleRepository.findByBoardId(boardDto.getId());
            if (CollectionUtils.isEmpty(persistedMockRules)) {
                return Collections.emptyList();
            }

            var dtos = persistedMockRules.stream()
                    .map(mockRule -> modelMapper.map(mockRule, MockRuleDto.class))
                    .toList();
            mockRuleCache.addMockRules(boardDto.getId(), dtos);
            return dtos;
        }
        return cachedMockRules;
    }

    public IdResponse updateMockRule(BoardDto boardDto, String mockRuleId, MockRuleDto mockRuleDto) {
        log.debug("updating mock rule={} for boardId={}", mockRuleId, boardDto.getId());
        mockRuleValidator.validateMockRule(mockRuleDto);

        var mockRuleDtos = getMockRules(boardDto);
        var existingDto = mockRuleDtos.stream()
                .filter(m -> m.getId().equals(mockRuleId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Mock rule not found for id: " + mockRuleId));

        existingDto.setMethod(mockRuleDto.getMethod());
        existingDto.setPath(mockRuleDto.getPath());
        existingDto.setHeaders(JsonUtils.minify(mockRuleDto.getHeaders()));
        existingDto.setBody(JsonUtils.minify(mockRuleDto.getBody()));
        existingDto.setStatusCode(mockRuleDto.getStatusCode());
        existingDto.setDelay(mockRuleDto.getDelay());
        mockRuleCache.updateMockRule(boardDto.getId(), existingDto);
        matchingEngineCache.invalidate(boardDto.getId());

        var mockRule = modelMapper.map(existingDto, MockRule.class);
        eventQueue.publish(DomainEvent.update(mockRule, mockRuleId, MockRule.class));
        return new IdResponse(mockRuleId);
    }

    public void deleteMockRule(BoardDto boardDto, String mockRuleId) {
        log.info("deleting mock rule={} for boardId={}", mockRuleId, boardDto.getId());
        mockRuleCache.deleteMockRule(boardDto.getId(), mockRuleId);
        matchingEngineCache.invalidate(boardDto.getId());
        eventQueue.publish(DomainEvent.delete(mockRuleId, MockRule.class));
    }
}
