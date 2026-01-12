package dev.mockboard.common.validator;

import dev.mockboard.common.domain.dto.MockRuleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static dev.mockboard.Constants.*;

@Component
@RequiredArgsConstructor
public class MockRuleValidator {

    private final ObjectMapper objectMapper;

    public void validateMockRule(MockRuleDto dto) {
        validatePath(dto.getPath());
        validateBody(dto.getBody());
        validateStatusCode(dto.getStatusCode());
        validateMethod(dto.getMethod());
        validateHeaders(dto.getHeaders());
        validateDelay(dto.getDelay());
    }

    private void validatePath(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path cannot be empty");
        }

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("Path must start with /");
        }

        if (path.length() > MAX_PATH_LENGTH) {
            throw new IllegalArgumentException("Path exceeds maximum length of " + MAX_PATH_LENGTH);
        }

        if (!VALID_PATH_PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException("Path contains invalid characters. Allowed: a-z, A-Z, 0-9, /, _, -, *");
        }

        long wildcardCount = path.chars().filter(ch -> ch == '*').count();
        if (wildcardCount > MAX_WILDCARDS) {
            throw new IllegalArgumentException("Path cannot have more than " + MAX_WILDCARDS + " wildcards");
        }

        // maybe in future
        if (path.contains("**")) {
            throw new IllegalArgumentException("Adjacent wildcards (**) not allowed");
        }
    }

    private void validateBody(String body) {
        if (body == null) {
            return;
        }

        try {
            objectMapper.readTree(body);
        } catch (Exception e) {
            throw new IllegalArgumentException("Body must be valid JSON string");
        }

        if (body.length() > MAX_BODY_LENGTH) {
            throw new IllegalArgumentException("Body too large (max " +  (MAX_BODY_LENGTH / 1000) + "KB)");
        }
    }

    private void validateStatusCode(int statusCode) {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("Invalid HTTP status code: " + statusCode);
        }
    }

    private void validateMethod(String method) {
        if (method == null || method.isEmpty()) {
            throw new IllegalArgumentException("HTTP method cannot be empty");
        }

        if (!VALID_HTTP_METHODS.contains(method.toUpperCase())) {
            throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }
    }

    private void validateHeaders(String headersString) {
        if (headersString == null) return;

        try {
            var headersMap = objectMapper.readValue(headersString, new TypeReference<Map<String, String>>() {});
            if (headersMap.size() > MAX_HEADERS_SIZE) {
                throw new IllegalArgumentException("Too many headers (max " + MAX_HEADERS_SIZE + " allowed)");
            }

            headersMap.forEach((key, value) -> {
                if (key.length() > MAX_HEADER_KEY_LENGTH || value.length() > MAX_HEADER_VALUE_LENGTH) {
                    throw new IllegalArgumentException("Header key or value too long");
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Headers must be valid JSON string");
        }
    }

    private void validateDelay(long delay) {
        if (delay < 0 || delay > 10_000) {
            throw new IllegalArgumentException("Delay must be in the range 0...10000");
        }
    }
}
