package dev.mockboard.service;

import dev.mockboard.Constants;
import dev.mockboard.common.cache.BoardCache;
import dev.mockboard.common.cache.MockRuleCache;
import dev.mockboard.common.cache.WebhookCache;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.exception.ForbiddenException;
import dev.mockboard.common.exception.NotFoundException;
import dev.mockboard.common.utils.IdGenerator;
import dev.mockboard.common.utils.StringUtils;
import dev.mockboard.repository.BoardRepository;
import dev.mockboard.repository.model.Board;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static dev.mockboard.Constants.BOARD_OWNER_TOKEN_LENGTH;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private final ModelMapper modelMapper;
    private final BoardRepository boardRepository;

    private final BoardCache boardCache;
    private final MockRuleCache mockRuleCache;
    private final WebhookCache webhookCache;

    @Transactional
    public BoardDto createBoard() {
        if (Constants.MAX_ACTIVE_BOARDS_CHECK_ENABLED) {
            var currentActiveBoards = boardCache.size();
            if (currentActiveBoards >= Constants.MAX_ACTIVE_BOARDS) {
                throw new ForbiddenException("Maximum number of active boards exceeded.");
            }
        }
        var boardId = IdGenerator.generateBoardId();
        var ownerToken = StringUtils.generate(BOARD_OWNER_TOKEN_LENGTH);

        var board = Board.builder()
                .id(boardId)
                .ownerToken(ownerToken)
                .timestamp(Instant.now())
                .build();
        var persisted = boardRepository.save(board);

        var boardDto = modelMapper.map(persisted, BoardDto.class);
        boardCache.put(persisted.getId(), boardDto);

        log.info("Created board: {}", persisted.getId());
        return boardDto;
    }

    @Transactional(readOnly = true)
    public BoardDto getBoardDto(String boardId) {
        var cachedOpt = boardCache.get(boardId);
        if (cachedOpt.isPresent()) {
            log.debug("Board cache hit: {}", boardId);
            return cachedOpt.get();
        }

        log.debug("Board cache miss: {}, fallback to DB", boardId);
        var boardOpt = boardRepository.findByIdAndDeletedFalse(boardId);
        if (boardOpt.isEmpty()) {
            throw new NotFoundException("Board not found by id: " + boardId);
        }

        var boardDto = modelMapper.map(boardOpt.get(), BoardDto.class);
        boardCache.put(boardDto.getId(), boardDto);
        return boardDto;
    }

    @Transactional
    public void deleteBoard(BoardDto boardDto) {
        log.info("Soft delete board: {}", boardDto.getId());

        boardCache.invalidate(boardDto.getId());
        mockRuleCache.invalidate(boardDto.getId());
        webhookCache.invalidate(boardDto.getId());

        boardRepository.markDeleted(boardDto.getId());
    }
}
