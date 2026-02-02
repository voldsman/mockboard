package dev.mockboard.service;

import dev.mockboard.repository.BoardRepository;
import dev.mockboard.repository.MockRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CleanupServiceMockTest {

    @Mock private BoardRepository boardRepository;
    @Mock private MockRuleRepository mockRuleRepository;

    @InjectMocks private CleanupService cleanupService;

    @Test
    void hardDeleteBoards() {
        when(boardRepository.hardDeleteMarkedBoards()).thenReturn(5);

        cleanupService.hardDeleteBoards();
        verify(boardRepository).hardDeleteMarkedBoards();
    }

    @Test
    void hardDeleteMockRules() {
        when(mockRuleRepository.hardDeleteMarkedRules()).thenReturn(10);

        cleanupService.hardDeleteMockRules();
        verify(mockRuleRepository).hardDeleteMarkedRules();
    }
}