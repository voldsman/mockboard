package dev.mockboard.service;

import dev.mockboard.Constants;
import dev.mockboard.common.cache.BoardCache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreBoardServiceMockTest {

    @Mock private BoardCache boardCache;

    private PreBoardService preBoardService;

    @BeforeEach
    void setUp() {
        preBoardService = new PreBoardService(boardCache);
    }

    @Test
    void getAppConfigs() {
        var activeBoards = 10L;
        when(boardCache.size()).thenReturn(activeBoards);

        var result = preBoardService.getAppConfigs();

        assertThat(result).isNotNull();
        assertThat(result).containsKeys("app", "validations", "boards");

        var appConfig = (Map<String, Object>) result.get("app");
        assertThat(appConfig).isNotNull();
        assertThat(appConfig.get("version")).isEqualTo(Constants.APP_VERSION);

        var boardsConfig = (Map<String, Object>) result.get("boards");
        assertThat(boardsConfig).isNotNull();
        assertThat(boardsConfig.get("activeBoards")).isEqualTo(activeBoards);
        assertThat(boardsConfig.get("maxActiveBoards")).isEqualTo(Constants.MAX_ACTIVE_BOARDS);

    }
}