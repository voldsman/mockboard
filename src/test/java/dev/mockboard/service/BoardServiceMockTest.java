package dev.mockboard.service;

import dev.mockboard.core.common.domain.dto.BoardDto;
import dev.mockboard.core.common.exception.NotFoundException;
import dev.mockboard.core.common.mapper.BoardMapper;
import dev.mockboard.storage.cache.BoardCacheStore;
import dev.mockboard.storage.doc.BoardDoc;
import dev.mockboard.storage.doc.repo.BoardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceMockTest {

    @Mock private BoardRepository boardRepository;
    @Mock private BoardCacheStore boardCacheStore;
    @Mock private BoardMapper boardMapper;
    @InjectMocks private BoardService boardService;

    @Test
    void createBoard_Success() {
        var savedDoc = BoardDoc.builder()
                .id("generated-id")
                .apiKey("generated-api-key")
                .ownerToken("generated-token")
                .createdAt(LocalDateTime.now())
                .build();

        var expectedDto = BoardDto.builder()
                .id("generated-id")
                .apiKey("generated-api-key")
                .build();

        when(boardRepository.save(any(BoardDoc.class))).thenReturn(savedDoc);
        when(boardMapper.mapBoardDocToBoardDto(savedDoc)).thenReturn(expectedDto);

        var result = boardService.createBoard();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("generated-id");
        assertThat(result.getApiKey()).isEqualTo("generated-api-key");

        verify(boardRepository, times(1)).save(any(BoardDoc.class));
        verify(boardCacheStore, times(1)).initBoardCache("generated-id", expectedDto);
    }

    @Test
    void getBoardDto_CacheHit() {
        var boardId = "board-123";
        var cachedDto = BoardDto.builder().id(boardId).build();

        when(boardCacheStore.getBoardCache(boardId)).thenReturn(cachedDto);

        var result = boardService.getBoardDto(boardId);
        assertThat(result).isEqualTo(cachedDto);
        verify(boardRepository, never()).findById(any());
    }

    @Test
    void getBoardDto_CacheMiss() {
        var boardId = "board-123";
        var doc = BoardDoc.builder().id(boardId).build();
        var dto = BoardDto.builder().id(boardId).build();

        when(boardCacheStore.getBoardCache(boardId)).thenReturn(null);
        when(boardRepository.findById(boardId)).thenReturn(Optional.of(doc));
        when(boardMapper.mapBoardDocToBoardDto(doc)).thenReturn(dto);

        var result = boardService.getBoardDto(boardId);

        assertThat(result.getId()).isEqualTo(boardId);
        verify(boardCacheStore).getBoardCache(boardId);
        verify(boardRepository).findById(boardId);
    }

    @Test
    void getBoardDto_NotFound() {
        var boardId = "missing-id";

        when(boardCacheStore.getBoardCache(boardId)).thenReturn(null);
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.getBoardDto(boardId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Board not found by id: missing-id");
    }
}