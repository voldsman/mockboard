package dev.mockboard.service;

import dev.mockboard.core.common.domain.dto.BoardDto;
import dev.mockboard.core.common.domain.dto.MockRuleDto;
import dev.mockboard.core.common.exception.NotFoundException;
import dev.mockboard.core.common.mapper.MockRuleMapper;
import dev.mockboard.core.common.validator.MockRuleValidator;
import dev.mockboard.storage.cache.MockExecutionCacheStore;
import dev.mockboard.storage.cache.MockRuleCacheStore;
import dev.mockboard.storage.doc.MockRuleDoc;
import dev.mockboard.storage.doc.repo.MockRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockRuleServiceMockTest {

    @Mock private BoardService boardService;
    @Mock private MockRuleValidator mockRuleValidator;
    @Mock private MockRuleRepository mockRuleRepository;
    @Mock private MockRuleMapper mockRuleMapper;
    @Mock private MockRuleCacheStore mockRuleCacheStore;
    @Mock private MockExecutionCacheStore mockExecutionCacheStore;
    @InjectMocks private MockRuleService mockRuleService;

    private final BoardDto boardDto = BoardDto.builder().id("board-1").apiKey("api-key-1").build();
    private final MockRuleDto mockRuleDto = MockRuleDto.builder().path("/test").method("GET").statusCode(200).build();
    private final MockRuleDoc mockRuleDoc = MockRuleDoc.builder().id("mock-1").boardId("board-1").apiKey("api-key-1").build();

    @Test
    void addMockRule_Success() {
        when(mockRuleMapper.mapMockRuleDtoToMockRuleDoc(mockRuleDto)).thenReturn(mockRuleDoc);
        when(mockRuleRepository.save(mockRuleDoc)).thenReturn(mockRuleDoc);

        var response = mockRuleService.addMockRule(boardDto, mockRuleDto);
        assertThat(response.id()).isEqualTo("mock-1");

        verify(mockRuleValidator).validateMockRule(mockRuleDto);
        verify(mockRuleRepository).save(mockRuleDoc);

        verify(mockRuleCacheStore).evict("api-key-1");
        verify(mockExecutionCacheStore).evict("api-key-1");
    }

    @Test
    void updateMockRule_Success() {
        var mockId = "mock-1";
        var updateDto = MockRuleDto.builder()
                .method("POST")
                .path("/updated")
                .statusCode(201)
                .body("{}")
                .build();

        when(mockRuleRepository.findByIdAndBoardId(mockId, boardDto.getId()))
                .thenReturn(Optional.of(mockRuleDoc));

        var response = mockRuleService.updateMockRule(boardDto, mockId, updateDto);
        assertThat(response.id()).isEqualTo(mockId);
        assertThat(mockRuleDoc.getMethod()).isEqualTo("POST");
        assertThat(mockRuleDoc.getPath()).isEqualTo("/updated");
        assertThat(mockRuleDoc.getStatusCode()).isEqualTo(201);

        verify(mockRuleValidator).validateMockRule(updateDto);
        verify(mockRuleRepository).save(mockRuleDoc);

        verify(mockRuleCacheStore).evict("api-key-1");
        verify(mockExecutionCacheStore).evict("api-key-1");
    }

    @Test
    void updateMockRule_NotFound() {
        when(mockRuleRepository.findByIdAndBoardId("missing-id", boardDto.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> mockRuleService.updateMockRule(boardDto, "missing-id", mockRuleDto))
                .isInstanceOf(NotFoundException.class);
        verify(mockRuleRepository, never()).save(any());
        verify(mockRuleCacheStore, never()).evict(any());
    }

    @Test
    void getMockRuleDtos_CacheHit() {
        when(mockRuleCacheStore.getMockRules(boardDto.getApiKey()))
                .thenReturn(List.of(mockRuleDto));

        var result = mockRuleService.getMockRuleDtos(boardDto);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(mockRuleDto);
        verify(mockRuleRepository, never()).findByBoardId(any());
    }

    @Test
    void getMockRuleDtos_CacheMiss() {
        when(mockRuleCacheStore.getMockRules(boardDto.getApiKey())).thenReturn(null);
        when(mockRuleRepository.findByBoardId(boardDto.getId())).thenReturn(List.of(mockRuleDoc));
        when(mockRuleMapper.mapMockRuleDocToMockRuleDto(mockRuleDoc)).thenReturn(mockRuleDto);

        var result = mockRuleService.getMockRuleDtos(boardDto);
        assertThat(result).hasSize(1);
        verify(mockRuleRepository).findByBoardId(boardDto.getId());
        verify(mockRuleCacheStore).initMockRulesCache(eq(boardDto.getApiKey()), anyList());
    }

    @Test
    void deleteMockRule_Success() {
        when(mockRuleRepository.findByIdAndBoardId("mock-1", boardDto.getId()))
                .thenReturn(Optional.of(mockRuleDoc));
        mockRuleService.deleteMockRule(boardDto, "mock-1");

        verify(mockRuleRepository).delete(mockRuleDoc);
        verify(mockRuleCacheStore).evict("api-key-1");
        verify(mockExecutionCacheStore).evict("api-key-1");
    }

    @Test
    void deleteMockRule_NotFound() {
        when(mockRuleRepository.findByIdAndBoardId("missing", boardDto.getId()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> mockRuleService.deleteMockRule(boardDto, "missing"))
                .isInstanceOf(NotFoundException.class);
        verify(mockRuleRepository, never()).delete(any());
    }
}