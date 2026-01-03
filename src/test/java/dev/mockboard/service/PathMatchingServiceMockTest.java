package dev.mockboard.service;

import dev.mockboard.core.common.domain.dto.MockRuleDto;
import dev.mockboard.core.engine.PathMatchingEngine;
import dev.mockboard.storage.cache.MockExecutionCacheStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PathMatchingServiceMockTest {

    @Mock private MockRuleService mockRuleService;
    @Mock private MockExecutionCacheStore mockExecutionCacheStore;
    @InjectMocks private PathMatchingService pathMatchingService;

    @Test
    void getMatchingMockRuleId_CacheHit() {
        var apiKey = "api-key";
        var path = "/cached-path";
        var expectedMockId = "mock-1";

        var mockedEngine = mock(PathMatchingEngine.class);
        when(mockedEngine.match(path)).thenReturn(Optional.of(expectedMockId));
        when(mockExecutionCacheStore.getEngineByApiKey(apiKey)).thenReturn(mockedEngine);

        var result = pathMatchingService.getMatchingMockRuleId(apiKey, path);
        assertThat(result).isPresent().contains(expectedMockId);
        verify(mockRuleService, never()).getMockRuleDtos(anyString());
        verify(mockExecutionCacheStore, never()).addEngineCache(anyString(), any());
    }

    @Test
    void getMatchingMockRuleId_CacheMiss() {
        var apiKey = "api-key";
        var path = "/api/users/123";
        var mockId = "mock-wildcard";

        when(mockExecutionCacheStore.getEngineByApiKey(apiKey)).thenReturn(null);
        var rule = MockRuleDto.builder()
                .id(mockId)
                .path("/api/users/*")
                .build();
        when(mockRuleService.getMockRuleDtos(apiKey)).thenReturn(List.of(rule));

        var result = pathMatchingService.getMatchingMockRuleId(apiKey, path);
        assertThat(result).isPresent().contains(mockId);
        verify(mockRuleService).getMockRuleDtos(apiKey);
        verify(mockExecutionCacheStore).addEngineCache(eq(apiKey), any(PathMatchingEngine.class));
    }

    @Test
    void getMatchingMockRuleId_NoMatch() {
        var apiKey = "api-key";
        when(mockExecutionCacheStore.getEngineByApiKey(apiKey)).thenReturn(null);

        var rule = MockRuleDto.builder().id("1").path("/known").build();
        when(mockRuleService.getMockRuleDtos(apiKey)).thenReturn(List.of(rule));

        var result = pathMatchingService.getMatchingMockRuleId(apiKey, "/unknown");
        assertThat(result).isEmpty();
    }

    @Test
    void buildEngine_Resilience() {
        var apiKey = "api-key";
        when(mockExecutionCacheStore.getEngineByApiKey(apiKey)).thenReturn(null);

        var badRule = MockRuleDto.builder().id("bad").path(null).build();
        var goodRule = MockRuleDto.builder().id("good").path("/good").build();

        when(mockRuleService.getMockRuleDtos(apiKey)).thenReturn(List.of(badRule, goodRule));

        var result = pathMatchingService.getMatchingMockRuleId(apiKey, "/good");
        assertThat(result).isPresent().contains("good");
    }
}