package dev.mockboard.core.common.validator;

import dev.mockboard.core.common.domain.dto.MockRuleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class MockRuleValidator {

    private static final Pattern VALID_PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9/_\\-*{}]+$");
    private static final Set<String> VALID_HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");

    private static final int MAX_PATH_LENGTH = 256;
    private static final int MAX_BODY_LENGTH = 5_000;
    private static final int MAX_WILDCARDS = 3;
    private static final int MAX_HEADERS_SIZE = 20;
    private static final int MAX_HEADER_KEY_SIZE = 100;
    private static final int MAX_HEADER_VALUE_SIZE = 500;

    private final ObjectMapper objectMapper;

    public void validateMockRule(MockRuleDto dto) {
        validatePath(dto.getPath());
        validateBody(dto.getBody());
        validateStatusCode(dto.getStatusCode());
        validateMethod(dto.getMethod());
        validateHeaders(dto.getHeaders());
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
        // get, options do not have a body
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
        // or make it GET by default
        if (method == null || method.isEmpty()) {
            throw new IllegalArgumentException("HTTP method cannot be empty");
        }

        if (!VALID_HTTP_METHODS.contains(method.toUpperCase())) {
            throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }
    }

    private void validateHeaders(Map<String, String> headers) {
        if (headers == null) return;

        if (headers.size() > MAX_HEADERS_SIZE) {
            throw new IllegalArgumentException("Too many headers (max " + MAX_HEADERS_SIZE + ")");
        }

        headers.forEach((key, value) -> {
            if (key.length() > MAX_HEADER_KEY_SIZE || value.length() > MAX_HEADER_VALUE_SIZE) {
                throw new IllegalArgumentException("Header key or value too long");
            }
        });
    }
}
