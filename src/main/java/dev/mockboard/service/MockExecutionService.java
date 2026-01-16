package dev.mockboard.service;

import dev.mockboard.Constants;
import dev.mockboard.cache.MockRuleCache;
import dev.mockboard.common.domain.MockExecutionResult;
import dev.mockboard.common.domain.RequestMetadata;
import dev.mockboard.common.domain.dto.MockRuleDto;
import dev.mockboard.common.engine.TemplateFakerEngine;
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

    private final MockRuleCache mockRuleCache;
    private final ObjectMapper objectMapper;
    private final PathMatchingService pathMatchingService;
    private final TemplateFakerService templateFakerService;

    public MockExecutionResult execute(String apiKey, RequestMetadata metadata) {
        var mockRule = findMatchingRule(apiKey, metadata.mockPath(), metadata.method()).orElse(null);
        var statusCode = mockRule != null ? mockRule.getStatusCode() : 200;
        var body = determineResponseBody(mockRule);
        var headers = buildHeaders(mockRule);
        applyDelay(mockRule);

        return new MockExecutionResult(mockRule, headers, body, statusCode);
    }

    private Optional<MockRuleDto> findMatchingRule(String apiKey, String path, String method) {
        var mockIdOpt = pathMatchingService.getMatchingMockRuleId(apiKey, path);
        if (mockIdOpt.isEmpty()) {
            log.debug("No mockId matching apiKey={} and path={} found", apiKey, path);
            return Optional.empty();
        }

        return getMockRule(apiKey, mockIdOpt.get(), method);
    }

    private Optional<MockRuleDto> getMockRule(String apiKey, String mockId, String method) {
        var cachedMocks = mockRuleCache.getMockRules(apiKey);
        if (!CollectionUtils.isEmpty(cachedMocks)) {
            var cached = cachedMocks.stream()
                    .filter(mock -> mock.getId().equals(mockId))
                    .filter(mock -> mock.getMethod().equalsIgnoreCase(method))
                    .findFirst();

            if (cached.isPresent()) {
                log.trace("Cache hit for mockId={}, method={}", mockId, method);
                return cached;
            }
        }
        return Optional.empty();
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
