package dev.mockboard.service;

import dev.mockboard.cache.MockRuleCache;
import dev.mockboard.common.domain.dto.MockRuleDto;
import dev.mockboard.common.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MockExecutionService {

    private final PathMatchingService pathMatchingService;
    private final MockRuleCache mockRuleCache;

    public record MockExecutionResult(
            MockRuleDto matchingMockRuleDto,
            HttpHeaders headers,
            String responseBody,
            int statusCode) {}

    public MockExecutionResult execute(String apiKey, HttpServletRequest request) {
        var mockPath = RequestUtils.extractMockPath(request, apiKey);
        var method = request.getMethod();

        var mockRule = findMatchingRule(apiKey, mockPath, method).orElse(null);
        int statusCode = 200;
        var body = "{\"message\": \"Hello from Mockboard.dev\"}";

        if (mockRule != null) {
            statusCode = mockRule.getStatusCode();
            body = mockRule.getBody();
            if (body == null || body.isEmpty()) {
                body = "{}"; // make it an empty json for now
            }
        }

        var headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        // simulate delay
        if (mockRule != null && mockRule.getDelay() > 0) {
            try {
                log.debug("Delaying [{}] for {}ms", Thread.currentThread(), mockRule.getDelay());
                Thread.sleep(Duration.ofMillis(mockRule.getDelay()));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
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
}
