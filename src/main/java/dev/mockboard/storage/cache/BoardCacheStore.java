package dev.mockboard.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import dev.mockboard.core.AppProperties;
import dev.mockboard.core.common.domain.dto.BoardDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BoardCacheStore extends BaseCacheStore<String, BoardDto> {

    // boardId as key, boardDto as value
    private final Cache<String, BoardDto> boards;

    public BoardCacheStore(AppProperties appProperties) {
        log.info("Initializing boards and boardsIdByApiKey cache...");
        this.boards = buildCache(appProperties);
    }

    public void initBoardCache(String boardId, BoardDto boardDto) {
        log.info("init board cache for boardId={}", boardId);
        boards.put(boardId, boardDto);
    }

    public BoardDto getBoardCache(String boardId) {
        return boards.getIfPresent(boardId);
    }

    public void updateBoardCache(String boardId, BoardDto boardDto) {
        log.info("update board cache for boardId={}", boardId);
        boards.put(boardId, boardDto);
    }

    @Override
    public void evict(String boardId) {
        boards.invalidate(boardId);
        log.debug("evict board cache for boardId={}", boardId);
    }

    @Override
    public void evictAll() {
        boards.invalidateAll();
        log.info("Evict all board entries");
    }
}
