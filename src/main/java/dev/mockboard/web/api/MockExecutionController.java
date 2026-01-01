package dev.mockboard.web.api;

import dev.mockboard.core.common.exception.NotFoundException;
import dev.mockboard.core.common.ratelimiter.MockExecutionRateLimiter;
import dev.mockboard.core.utils.RequestUtils;
import dev.mockboard.service.MockExecutionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/m/{apiKey}")
@RequiredArgsConstructor
public class MockExecutionController {

    private final MockExecutionService mockExecutionService;
    private final MockExecutionRateLimiter mockExecutionRateLimiter;

    @RequestMapping(value = "/**", method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.OPTIONS,
            RequestMethod.PATCH,})
    public ResponseEntity<String> executeMock(@PathVariable String apiKey, HttpServletRequest request) {
        mockExecutionRateLimiter.checkLimit(apiKey);

        var mockPath = RequestUtils.extractMockPath(request, apiKey);
        var method = request.getMethod();

        // todo: when empty send empty response, not exception
        var mockRule = mockExecutionService.findMatchingRule(apiKey, mockPath, method)
                .orElseThrow(() -> new NotFoundException(
                        "No mock found for " + method + " " + mockPath
                ));

        var headers = new HttpHeaders();
        if (mockRule.getHeaders() != null && !mockRule.getHeaders().isEmpty()) {
            mockRule.getHeaders().forEach(headers::add);
        }

        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return ResponseEntity
                .status(mockRule.getStatusCode())
                .headers(headers)
                .body(mockRule.getBody());
    }
}
