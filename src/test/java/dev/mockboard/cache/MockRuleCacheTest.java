package dev.mockboard.cache;

import dev.mockboard.common.cache.MockRuleCache;
import dev.mockboard.common.domain.dto.MockRuleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MockRuleCacheTest {

    private MockRuleCache mockRuleCache;
    private final String API_KEY = "test-api-key";

    @BeforeEach
    void setUp() {
        mockRuleCache = new MockRuleCache();
    }

    @Test
    void addMockRules() {
        var oldRule = createDto("1");
        var newRule = createDto("2");

        mockRuleCache.addMockRules(API_KEY, List.of(oldRule, newRule));
        var result = mockRuleCache.getMockRules(API_KEY);
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo("2");
    }

    @Test
    void addMockRule() {
        var rule = createDto("1");
        mockRuleCache.addMockRule(API_KEY, rule);

        assertThat(mockRuleCache.getMockRules(API_KEY)).hasSize(1);
    }

    @Test
    void updateMockRule() {
        var ruleId = "update-id";
        var mockRuleDto = createDto(ruleId);
        mockRuleCache.addMockRule(API_KEY, mockRuleDto);

        var updatedMockRuleDto = createDto(ruleId);
        updatedMockRuleDto.setMethod("PATCH");
        mockRuleCache.updateMockRule(API_KEY, updatedMockRuleDto);

        var result = mockRuleCache.getMockRules(API_KEY);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(mockRuleDto.getId());
        assertThat(result.getFirst().getMethod()).isEqualTo("PATCH");
        assertThat(result.getFirst().getTimestamp()).isEqualTo(updatedMockRuleDto.getTimestamp());
    }

    @Test
    void createMockRuleIfKeyIsEmpty() {
        var newRule = createDto("new-id");
        mockRuleCache.updateMockRule("unknown-key", newRule);

        assertThat(mockRuleCache.getMockRules("unknown-key")).hasSize(1);
    }

    @Test
    void deleteMockRule() {
        mockRuleCache.addMockRule(API_KEY, createDto("id-1"));
        mockRuleCache.addMockRule(API_KEY, createDto("id-2"));

        mockRuleCache.deleteMockRule(API_KEY, "id-1");

        var result = mockRuleCache.getMockRules(API_KEY);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo("id-2");
    }

    private MockRuleDto createDto(String id) {
        var dto = new MockRuleDto();
        dto.setId(id);
        dto.setBoardId("board-id");
        dto.setMethod("GET");
        dto.setTimestamp(Instant.now());
        return dto;
    }
}