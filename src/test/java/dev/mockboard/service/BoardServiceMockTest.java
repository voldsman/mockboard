package dev.mockboard.service;

import dev.mockboard.Constants;
import dev.mockboard.common.cache.BoardCache;
import dev.mockboard.common.cache.MockRuleCache;
import dev.mockboard.common.cache.WebhookCache;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.exception.NotFoundException;
import dev.mockboard.repository.BoardRepository;
import dev.mockboard.repository.model.Board;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceMockTest {

    @Mock private ModelMapper modelMapper;
    @Mock private BoardRepository boardRepository;
    @Mock private BoardCache boardCache;
    @Mock private MockRuleCache mockRuleCache;
    @Mock private WebhookCache webhookCache;

    @InjectMocks private BoardService boardService;

    @Test
    void createBoard() {
        var board = Board.builder()
                .id("board-123")
                .ownerToken("t".repeat(48))
                .timestamp(Instant.now())
                .build();

        var boardDto = BoardDto.builder()
                .id("board-123")
                .ownerToken("t".repeat(48))
                .build();

        when(boardRepository.save(any(Board.class))).thenReturn(board);
        when(modelMapper.map(board, BoardDto.class)).thenReturn(boardDto);

        var result = boardService.createBoard();
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOwnerToken()).isNotNull();
        assertThat(result.getOwnerToken()).hasSize(Constants.BOARD_OWNER_TOKEN_LENGTH);

        verify(boardRepository).save(any(Board.class));
        verify(boardCache).put(eq(board.getId()), any(BoardDto.class));
    }

    @Test
    void getBoardDto_cacheHit() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder()
                .id(boardId)
                .ownerToken("token-abc")
                .build();

        when(boardCache.get(boardId)).thenReturn(Optional.of(boardDto));

        var result = boardService.getBoardDto(boardId);
        assertThat(result).isEqualTo(boardDto);
        verify(boardRepository, never()).findByIdAndDeletedFalse(any());
    }

    @Test
    void getBoardDto_cacheMiss() {
        var boardId = "board-123";
        var board = Board.builder()
                .id(boardId)
                .ownerToken("token-abc")
                .timestamp(Instant.now())
                .build();

        var boardDto = BoardDto.builder()
                .id(boardId)
                .ownerToken("token-abc")
                .build();

        when(boardCache.get(boardId)).thenReturn(Optional.empty());
        when(boardRepository.findByIdAndDeletedFalse(boardId)).thenReturn(Optional.of(board));
        when(modelMapper.map(board, BoardDto.class)).thenReturn(boardDto);

        var result = boardService.getBoardDto(boardId);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(boardId);
        verify(boardCache).put(boardId, boardDto);
    }

    @Test
    void getBoardDto_notFound() {
        var boardId = "non-existent-board";

        when(boardCache.get(boardId)).thenReturn(Optional.empty());
        when(boardRepository.findByIdAndDeletedFalse(boardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.getBoardDto(boardId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Board not found by id: " + boardId);
    }

    @Test
    void deleteBoard() {
        var boardId = "board-123";
        var boardDto = BoardDto.builder()
                .id(boardId)
                .ownerToken("token-abc")
                .build();

        boardService.deleteBoard(boardDto);

        verify(boardCache).invalidate(boardId);
        verify(mockRuleCache).invalidate(boardId);
        verify(webhookCache).invalidate(boardId);
        verify(boardRepository).markDeleted(boardId);
    }
}