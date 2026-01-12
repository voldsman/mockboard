package dev.mockboard.web.api;

import dev.mockboard.service.MockExecutionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/m/{apiKey}")
@RequiredArgsConstructor
public class MockExecutionController {

    private final MockExecutionService mockExecutionService;

    @RequestMapping(value = "/**", method = {
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT,
            RequestMethod.PATCH,
            RequestMethod.DELETE,
            RequestMethod.OPTIONS,
            RequestMethod.HEAD,
    })
    public ResponseEntity<String> executeMock(@PathVariable String apiKey, HttpServletRequest request) {
        var executionStart = System.currentTimeMillis();
        var result = mockExecutionService.execute(apiKey, request);

        var executionTime = System.currentTimeMillis() - executionStart;
        log.debug("Execution time: {}ms", executionTime);
        return ResponseEntity
                .status(result.statusCode())
                .headers(result.headers())
                .body(result.responseBody());
    }
}
