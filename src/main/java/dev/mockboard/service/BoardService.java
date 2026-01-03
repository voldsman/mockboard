package dev.mockboard.service;

import dev.mockboard.core.common.domain.dto.BoardDto;
import dev.mockboard.core.common.exception.NotFoundException;
import dev.mockboard.core.common.mapper.BoardMapper;
import dev.mockboard.core.utils.StringUtils;
import dev.mockboard.storage.cache.BoardCacheStore;
import dev.mockboard.storage.doc.BoardDoc;
import dev.mockboard.storage.doc.repo.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoardService {

    private static final int API_KEY_LENGTH = 16;
    private static final int OWNER_TOKEN_LENGTH = 32;

    private final BoardRepository boardRepository;
    private final BoardCacheStore boardCacheStore;
    private final BoardMapper boardMapper;

    public BoardDto createBoard() {
        var boardDoc = new BoardDoc();
        boardDoc.setApiKey(StringUtils.generate(API_KEY_LENGTH));
        boardDoc.setOwnerToken(StringUtils.generate(OWNER_TOKEN_LENGTH));
        boardDoc.setCreatedAt(LocalDateTime.now());
        var stored = boardRepository.save(boardDoc);
        boardCacheStore.initBoardCache(boardDoc.getId(), boardMapper.mapBoardDocToBoardDto(stored));
        return boardMapper.mapBoardDocToBoardDto(stored);
    }

    public BoardDto getBoardDto(String boardId) {
        var cachedBoard = boardCacheStore.getBoardCache(boardId);
        if (cachedBoard == null) {
            // fallback to db
            log.debug("Board {} not found in cache, fallback.", boardId);
            var boardDoc = getBoardDoc(boardId);
            return boardMapper.mapBoardDocToBoardDto(boardDoc);
        }

        return cachedBoard;
    }

    private BoardDoc getBoardDoc(String boardId) {
        var boardDocOpt = boardRepository.findById(boardId);
        if (boardDocOpt.isEmpty()) {
            throw new NotFoundException("Board not found by id: " + boardId);
        }

        return boardDocOpt.get();
    }
}
