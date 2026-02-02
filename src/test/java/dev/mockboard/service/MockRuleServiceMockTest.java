package dev.mockboard.service;

import dev.mockboard.Constants;
import dev.mockboard.common.cache.MockRuleCache;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.domain.dto.MockRuleDto;
import dev.mockboard.common.exception.BadRequestException;
import dev.mockboard.common.exception.NotFoundException;
import dev.mockboard.common.validator.MockRuleValidator;
import dev.mockboard.repository.MockRuleRepository;
import dev.mockboard.repository.model.MockRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockRuleServiceMockTest {

    @Mock private ModelMapper modelMapper;
    @Mock private MockRuleValidator mockRuleValidator;
    @Mock private MockRuleRepository mockRuleRepository;
    @Mock private MockRuleCache mockRuleCache;

    @InjectMocks private MockRuleService mockRuleService;

    @Test
    void createMockRule() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder().id(boardId).build();

        var mockRuleDto = new MockRuleDto();
        mockRuleDto.setMethod("GET");
        mockRuleDto.setPath("/api/test");
        mockRuleDto.setHeaders("{\"Content-Type\": \"application/json\"}");
        mockRuleDto.setBody("{\"key\": \"value\"}");
        mockRuleDto.setStatusCode(200);

        var mockRule = new MockRule();
        mockRule.setId("rule-1");

        when(mockRuleCache.getMockRules(boardId)).thenReturn(Collections.emptyList());
        when(modelMapper.map(any(MockRuleDto.class), eq(MockRule.class))).thenReturn(mockRule);
        when(mockRuleRepository.save(any(MockRule.class))).thenReturn(mockRule);

        var result = mockRuleService.createMockRule(boardDto, mockRuleDto);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("rule-1");

        verify(mockRuleValidator).validateMockRule(mockRuleDto);
        verify(mockRuleCache).addMockRule(eq(boardId), any(MockRuleDto.class));
        verify(mockRuleRepository).save(any(MockRule.class));
    }

    @Test
    void createMockRule_maxRulesExceeded() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder().id(boardId).build();
        var mockRuleDto = new MockRuleDto();

        var existingRules = new ArrayList<MockRuleDto>();
        for (int i = 0; i < Constants.MAX_MOCK_RULES; i++) {
            existingRules.add(new MockRuleDto());
        }

        when(mockRuleCache.getMockRules(boardId)).thenReturn(existingRules);

        assertThatThrownBy(() -> mockRuleService.createMockRule(boardDto, mockRuleDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Maximum number of mock rules exceeded");

        verify(mockRuleValidator, never()).validateMockRule(any());
        verify(mockRuleRepository, never()).save(any());
    }

    @Test
    void getMockRules_cacheHit() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder().id(boardId).build();

        var cachedRule = new MockRuleDto();
        cachedRule.setId("rule-1");
        cachedRule.setBoardId(boardId);

        when(mockRuleCache.getMockRules(boardId)).thenReturn(List.of(cachedRule));

        var result = mockRuleService.getMockRules(boardDto);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("rule-1");
        verify(mockRuleRepository, never()).findByBoardIdAndDeletedFalseOrderByTimestampDesc(any());
    }

    @Test
    void getMockRules_cacheMiss() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder().id(boardId).build();

        var mockRule = new MockRule();
        mockRule.setId("rule-1");
        mockRule.setBoardId(boardId);
        mockRule.setMethod("GET");
        mockRule.setPath("/api/test");

        var mockRuleDto = new MockRuleDto();
        mockRuleDto.setId("rule-1");
        mockRuleDto.setBoardId(boardId);
        mockRuleDto.setMethod("GET");
        mockRuleDto.setPath("/api/test");

        when(mockRuleCache.getMockRules(boardId)).thenReturn(Collections.emptyList());
        when(mockRuleRepository.findByBoardIdAndDeletedFalseOrderByTimestampDesc(boardId)).thenReturn(List.of(mockRule));
        when(modelMapper.map(mockRule, MockRuleDto.class)).thenReturn(mockRuleDto);

        var result = mockRuleService.getMockRules(boardDto);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("rule-1");
        verify(mockRuleCache).addMockRules(eq(boardId), any());
    }

    @Test
    void updateMockRule() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder().id(boardId).build();
        var mockRuleId = "rule-1";

        var existingDto = new MockRuleDto();
        existingDto.setId(mockRuleId);
        existingDto.setBoardId(boardId);
        existingDto.setMethod("GET");
        existingDto.setPath("/api/old");

        var updateDto = new MockRuleDto();
        updateDto.setMethod("POST");
        updateDto.setPath("/api/new");
        updateDto.setHeaders("{\"Auth\": \"Bearer token\"}");
        updateDto.setBody("{\"updated\": true}");
        updateDto.setStatusCode(201);
        updateDto.setDelay(100);

        var mockRule = new MockRule();
        mockRule.setId(mockRuleId);

        when(mockRuleCache.getMockRules(boardId)).thenReturn(List.of(existingDto));
        when(modelMapper.map(any(MockRuleDto.class), eq(MockRule.class))).thenReturn(mockRule);
        when(mockRuleRepository.save(any(MockRule.class))).thenReturn(mockRule);

        var result = mockRuleService.updateMockRule(boardDto, mockRuleId, updateDto);

        assertThat(result.id()).isEqualTo(mockRuleId);
        assertThat(existingDto.getMethod()).isEqualTo("POST");
        assertThat(existingDto.getPath()).isEqualTo("/api/new");
        assertThat(existingDto.getStatusCode()).isEqualTo(201);
        assertThat(existingDto.getDelay()).isEqualTo(100);

        verify(mockRuleValidator).validateMockRule(updateDto);
        verify(mockRuleCache).updateMockRule(boardId, existingDto);
        verify(mockRuleRepository).save(mockRule);
    }

    @Test
    void updateMockRule_ruleNotFound() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder().id(boardId).build();
        var mockRuleId = "non-existent-rule";
        var updateDto = new MockRuleDto();

        when(mockRuleCache.getMockRules(boardId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> mockRuleService.updateMockRule(boardDto, mockRuleId, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Mock rule not found for id: " + mockRuleId);

        verify(mockRuleRepository, never()).save(any());
    }

    @Test
    void deleteMockRule() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder().id(boardId).build();
        var mockRuleId = "rule-1";

        var existingDto = new MockRuleDto();
        existingDto.setId(mockRuleId);
        existingDto.setBoardId(boardId);

        when(mockRuleCache.getMockRules(boardId)).thenReturn(List.of(existingDto));

        mockRuleService.deleteMockRule(boardDto, mockRuleId);

        verify(mockRuleCache).deleteMockRule(boardId, mockRuleId);
        verify(mockRuleRepository).markDeleted(mockRuleId);
    }
}