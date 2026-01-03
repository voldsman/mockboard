package dev.mockboard.service;

import dev.mockboard.core.common.domain.dto.BoardDto;
import dev.mockboard.core.common.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardSecurityServiceMockTest {

    @Mock private BoardService boardService;
    @InjectMocks private BoardSecurityService boardSecurityService;

    @Test
    void validateOwnership_Success() {
        var boardId = "board-123";
        var validToken = "secret-token-123";
        var boardDto = BoardDto.builder()
                .id(boardId)
                .ownerToken(validToken)
                .build();

        when(boardService.getBoardDto(boardId)).thenReturn(boardDto);

        var result = boardSecurityService.validateOwnership(boardId, validToken);
        assertThat(result).isEqualTo(boardDto);
    }

    @Test
    void validateOwnership_Unauthorized() {
        var boardId = "board-123";
        var actualToken = "real-secret";
        var hackingAttemptToken = "wrong-secret";

        var boardDto = BoardDto.builder()
                .id(boardId)
                .ownerToken(actualToken)
                .build();

        when(boardService.getBoardDto(boardId)).thenReturn(boardDto);
        assertThatThrownBy(() -> boardSecurityService.validateOwnership(boardId, hackingAttemptToken))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid owner token");
    }
}