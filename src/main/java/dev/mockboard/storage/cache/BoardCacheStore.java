package dev.mockboard.storage.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.mockboard.core.AppProperties;
import dev.mockboard.core.common.domain.dto.BoardDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class BoardCacheStore extends BaseCacheStore<String, BoardDto> {

    // boardId as key, boardDto as value
    private final Cache<String, BoardDto> boards;

    // helper cache for mapping to avoid
    // apiKey as key, boardId as value (for mock execution lookups)
    private final Cache<String, String> boardsIdByApiKey;

    public BoardCacheStore(AppProperties appProperties) {
        log.info("Initializing boards and boardsIdByApiKey cache...");
        this.boards = buildCache(appProperties);

        this.boardsIdByApiKey = Caffeine.newBuilder()
                .maximumSize(appProperties.getMaxCacheEntries())
                .expireAfterAccess(Duration.ofMinutes(appProperties.getCacheExpireAfterAccessMinutes()))
                .build();
    }

    public void initBoardCache(String boardId, BoardDto boardDto) {
        log.info("init board cache for boardId={}", boardId);
        boards.put(boardId, boardDto);
        boardsIdByApiKey.put(boardDto.getApiKey(), boardDto.getId());
    }

    public void initBoardIdByApiKey(String boardId, String apiKey) {
        log.debug("init board cache for boardId={} by apiKey={}", boardId, apiKey);
        boardsIdByApiKey.put(boardId, apiKey);
    }

    public BoardDto getBoardCache(String boardId) {
        return boards.getIfPresent(boardId);
    }

    public BoardDto getBoardCacheByApiKey(String apiKey) {
        var boardId = getBoardIdByApiKey(apiKey);
        if (boardId == null) return null;
        return getBoardCache(boardId);
    }

    public String getBoardIdByApiKey(String apiKey) {
        return boardsIdByApiKey.getIfPresent(apiKey);
    }

    public void updateBoardCache(String boardId, BoardDto boardDto) {
        log.info("update board cache for boardId={}", boardId);
        boards.put(boardId, boardDto);
    }

    @Override
    public void evict(String boardId) {
        var boardCache = getBoardCache(boardId);
        boardsIdByApiKey.invalidate(boardCache.getApiKey());
        boards.invalidate(boardId);
    }

    @Override
    public void evictAll() {
        boards.invalidateAll();
        boardsIdByApiKey.invalidateAll();
        log.info("Evict all board entries");
    }
}
