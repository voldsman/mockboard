package dev.mockboard.web.api;

import dev.mockboard.common.utils.RequestUtils;
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

    @RequestMapping(value = "/**", method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.OPTIONS,
            RequestMethod.PATCH,})
    public ResponseEntity<String> executeMock(@PathVariable String apiKey, HttpServletRequest request) {
        var mockPath = RequestUtils.extractMockPath(request, apiKey);
        var method = request.getMethod();

        // todo: make it configurable to strict match or default response
        var mockRule = mockExecutionService.findMatchingRule(apiKey, mockPath, method)
                .orElse(null);

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
//        if (mockRule.getHeaders() != null && !mockRule.getHeaders().isEmpty()) {
//            mockRule.getHeaders().forEach(headers::add);
//        }

        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return ResponseEntity
                .status(statusCode)
                .headers(headers)
                .body(body);
    }
}
