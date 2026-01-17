package dev.mockboard.service;

import dev.mockboard.Constants;
import dev.mockboard.common.domain.MockExecutionResult;
import dev.mockboard.common.domain.RequestMetadata;
import dev.mockboard.common.domain.dto.BoardDto;
import dev.mockboard.common.domain.dto.MockRuleDto;
import dev.mockboard.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockExecutionService {

    private final ObjectMapper objectMapper;
    private final MockRuleService mockRuleService;
    private final TemplateFakerService templateFakerService;

    public MockExecutionResult execute(String apiKey, RequestMetadata metadata) {
        // it is safe, unless mockRuleService.getMockRules(boardDto) changes
        var boardDto = BoardDto.builder().id(apiKey).build();
        var mockRule = findMatchingRule(boardDto, metadata.mockPath(), metadata.method()).orElse(null);
        var statusCode = mockRule != null ? mockRule.getStatusCode() : 200;
        var body = determineResponseBody(mockRule);
        var headers = buildHeaders(mockRule);
        applyDelay(mockRule);

        return new MockExecutionResult(mockRule, headers, body, statusCode);
    }

    private Optional<MockRuleDto> findMatchingRule(BoardDto boardDto, String path, String method) {
        var mockRules = mockRuleService.getMockRules(boardDto);
        if (CollectionUtils.isEmpty(mockRules)) {
            return Optional.empty();
        }
        return mockRules.stream()
                .filter(r -> r.getMethod().equalsIgnoreCase(method))
                .filter(r -> r.matches(path))
                .min((r1, r2) -> {
                    int wld1 = StringUtils.countWildcards(r1.getPath());
                    int wld2 = StringUtils.countWildcards(r2.getPath());
                    if (wld1 != wld2) return Integer.compare(wld1, wld2);
                    return Integer.compare(r2.getPath().length(), r1.getPath().length());
                });
    }

    private String determineResponseBody(MockRuleDto mockRule) {
        if (mockRule == null) {
            return Constants.DEFAULT_EXECUTION_RESPONSE;
        }

        var body = mockRule.getBody();
        return (body == null || body.isEmpty())
                ? "{}"
                : templateFakerService.processTemplates(body);
    }

    private HttpHeaders buildHeaders(MockRuleDto mockRule) {
        var headers = new HttpHeaders();
        if (mockRule != null && mockRule.getHeaders() != null && !mockRule.getHeaders().isEmpty()) {
            try {
                var typeRef = new TypeReference<Map<String, String>>() {};
                var headersMap = objectMapper.readValue(mockRule.getHeaders(), typeRef);
                headersMap.forEach(headers::add);
            } catch (Exception e) {
                log.warn("Failed to parse headers, using default", e);
                headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            }
        } else {
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        }

        return headers;
    }

    private void applyDelay(MockRuleDto mockRule) {
        if (mockRule != null && mockRule.getDelay() > 0) {
            try {
                log.debug("Delaying [{}] for {}ms", Thread.currentThread(), mockRule.getDelay());
                Thread.sleep(mockRule.getDelay());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.warn("Delay interrupted", ex);
            }
        }
    }
}
