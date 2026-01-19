package dev.mockboard.common.domain.dto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class MockRuleDtoTest {

    @Test
    void exactPathMatch() {
        var rule = createRule("/api/v1/users");
        assertThat(rule.matches("/api/v1/users")).isTrue();
    }

    @Test
    void noMatchPrefix() {
        var rule = createRule("/users");
        assertThat(rule.matches("/api/v1/users")).isFalse();
    }

    @Test
    void noMatchSuffix() {
        var rule = createRule("/api/v1/users");
        assertThat(rule.matches("/api/v1/users/123")).isFalse();
    }

    @Test
    void matchSingleWildcard() {
        var rule = createRule("/api/v1/users/*/profile");
        assertThat(rule.matches("/api/v1/users/123/profile")).isTrue();
        assertThat(rule.matches("/api/v1/users/hello/profile")).isTrue();
    }

    @Test
    void noMatchWildcardMissing() {
        var rule = createRule("/api/v1/users/*/profile");
        assertThat(rule.matches("/api/v1/users/profile")).isFalse();
    }

    @Test
    void noMatchWildcardMatchesMultipleSegments() {
        var rule = createRule("/api/v1/users/*/profile");
        assertThat(rule.matches("/api/v1/users/123/settings/profile")).isFalse();
    }

    @Test
    void matchWildcardAtEnd() {
        var rule = createRule("/files/*");
        assertThat(rule.matches("/files/image.png")).isTrue();
        assertThat(rule.matches("/files/data.csv")).isTrue();
        assertThat(rule.matches("/files/users/logo.png")).isFalse();
    }

    @Test
    void matchLiteralsInPath() {
        var rule = createRule("/api/v1.0/users");
        assertThat(rule.matches("/api/v1.0/users")).isTrue();
        assertThat(rule.matches("/api/v1v0/users")).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            "/api/*/users/*/data,       /api/v1/users/123/data,     true",
            "/api/*/users/*/data,       /api/v1/users/123/info,     false",
            "/*/test/*,                 /123/test/456,              true",
            "/*/test/*,                 /123/test,                  false",
            "/api/**/hello,             /api/literally/star/hello,  false",
            "/api/*/*/*/folder,         /api/v1/2026/01/folder,     true"
    })
    void matchCases(String mockPath, String requestPath, boolean expectedResult) {
        var rule = createRule(mockPath);
        assertThat(rule.matches(requestPath)).isEqualTo(expectedResult);
    }

    private MockRuleDto createRule(String path) {
        var dto = new MockRuleDto();
        dto.setPath(path);
        dto.compilePattern();
        return dto;
    }
}