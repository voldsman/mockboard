package dev.mockboard.service;

import dev.mockboard.core.common.domain.dto.BoardDto;
import dev.mockboard.core.common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardSecurityService {

    private final BoardService boardService;

    public BoardDto validateOwnership(String boardId, String requestOwnerToken) {
        var boardDto = boardService.getBoardDto(boardId);
        if (!boardDto.getOwnerToken().equals(requestOwnerToken)) {
            throw new UnauthorizedException("Invalid owner token");
        }

        return boardDto;
    }
}
