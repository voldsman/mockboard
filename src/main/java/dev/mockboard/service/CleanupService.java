package dev.mockboard.service;

import dev.mockboard.repository.BoardRepository;
import dev.mockboard.repository.MockRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final BoardRepository boardRepository;
    private final MockRuleRepository mockRuleRepository;

    @Transactional
    @Scheduled(fixedRate = 3_600_000) // Runs every 1 hour
    public void hardDeleteBoards() {
        int deleted = boardRepository.hardDeleteMarkedBoards();
        log.info("Hard deleted {} soft-deleted boards", deleted);
    }

    @Transactional
    @Scheduled(fixedRate = 600_000) // Runs every 10 minutes
    public void hardDeleteMockRules() {
        int deleted = mockRuleRepository.hardDeleteMarkedRules();
        log.info("Hard deleted {} soft-deleted mock rules", deleted);
    }
}
