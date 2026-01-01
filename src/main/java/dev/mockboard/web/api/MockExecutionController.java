package dev.mockboard.web.api;

import dev.mockboard.service.MockExecutionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/m/{apiKey}/**")
@RequiredArgsConstructor
public class MockExecutionController {

    private final MockExecutionService mockExecutionService;

    @RequestMapping(value = "**", method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.DELETE,
            RequestMethod.OPTIONS,
            RequestMethod.PATCH,})
    public ResponseEntity<String> executeMock(@PathVariable String apiKey, HttpServletRequest request) {
//        String path = extractPath(request);
//        var mockRule = mockService.findMatchingRule(apiKey, path)
//                .orElseThrow(() -> new NotFoundException("No mock rule matches this path"));
//
//        return ResponseEntity
//                .status(mockRule.getStatusCode())
//                .headers(buildHeaders(mockRule.getHeaders()))
//                .body(mockRule.getBody());
        return null;
    }
}
