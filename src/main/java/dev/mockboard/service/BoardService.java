package dev.mockboard.service;

import dev.mockboard.common.cache.BoardCache;
import dev.mockboard.common.cache.MockRuleCache;
import dev.mockboard.common.cache.WebhookCache;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.exception.NotFoundException;
import dev.mockboard.common.utils.IdGenerator;
import dev.mockboard.common.utils.StringUtils;
import dev.mockboard.event.DomainEvent;
import dev.mockboard.event.EventQueue;
import dev.mockboard.repository.BoardRepository;
import dev.mockboard.repository.model.Board;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static dev.mockboard.Constants.BOARD_OWNER_TOKEN_LENGTH;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final EventQueue eventQueue;
    private final ModelMapper modelMapper;
    private final BoardRepository boardRepository;

    private final BoardCache boardCache;
    private final MockRuleCache mockRuleCache;
    private final WebhookCache webhookCache;

    public BoardDto createBoard() {
        var boardId = IdGenerator.generateBoardId();
        var ownerToken = StringUtils.generate(BOARD_OWNER_TOKEN_LENGTH);

        var board = Board.builder()
                .id(boardId)
                .ownerToken(ownerToken)
                .timestamp(Instant.now())
                .build();
        var boardDto = modelMapper.map(board, BoardDto.class);
        boardCache.put(board.getId(), boardDto);

        eventQueue.publish(DomainEvent.create(board, Board.class));
        log.info("Created board: {}", board.getId());
        return boardDto;
    }

    public BoardDto getBoardDto(String boardId) {
        var cachedOpt = boardCache.get(boardId);
        if (cachedOpt.isPresent()) {
            log.debug("Board cache hit: {}", boardId);
            return cachedOpt.get();
        }

        log.debug("Board cache miss: {}, fallback to DB", boardId);
        var boardOpt = boardRepository.findById(boardId);
        if (boardOpt.isEmpty()) {
            throw new NotFoundException("Board not found by id: " + boardId);
        }

        var boardDto = modelMapper.map(boardOpt.get(), BoardDto.class);
        boardCache.put(boardDto.getId(), boardDto);
        return boardDto;
    }

    public void deleteBoard(BoardDto boardDto) {
        log.info("Deleting board: {}", boardDto.getId());

        boardCache.invalidate(boardDto.getId());
        mockRuleCache.invalidate(boardDto.getId());
        webhookCache.invalidate(boardDto.getId());

        eventQueue.publish(DomainEvent.delete(boardDto.getId(), Board.class));
    }
}
