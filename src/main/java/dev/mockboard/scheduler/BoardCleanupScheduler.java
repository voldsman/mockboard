package dev.mockboard.scheduler;

import dev.mockboard.storage.cache.BoardCacheStore;
import dev.mockboard.storage.cache.MockExecutionCacheStore;
import dev.mockboard.storage.cache.MockRuleCacheStore;
import dev.mockboard.storage.repo.BoardRepository;
import dev.mockboard.storage.repo.MockRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoardCleanupScheduler {

    private final BoardRepository boardRepository;
    private final MockRuleRepository mockRuleRepository;

    private final BoardCacheStore boardCacheStore;
    private final MockRuleCacheStore mockRuleCacheStore;
    private final MockExecutionCacheStore mockExecutionCacheStore;

    // Delete all DB data and cache at 03:00 UTC
    @Scheduled(cron = "0 0 3 * * * ")
    public void cleanupBoards() {
        log.info("Starting daily cleanup...");
        var boardsCount = boardRepository.count();
        var mockRulesCount = mockRuleRepository.count();

        boardRepository.deleteAll();
        mockRuleRepository.deleteAll();

        boardCacheStore.evictAll();
        mockRuleCacheStore.evictAll();
        mockExecutionCacheStore.evictAll();
        log.info("Daily cleanup complete. Deleted {} boards and {} mock rules", boardsCount, mockRulesCount);
    }
}
