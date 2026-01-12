package dev.mockboard.common.domain;

import dev.mockboard.common.domain.dto.MockRuleDto;
import org.springframework.http.HttpHeaders;

public record MockExecutionResult(
        MockRuleDto matchingMockRuleDto,
        HttpHeaders headers,
        String responseBody,
        int statusCode) {}
