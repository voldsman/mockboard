package dev.mockboard.common.validator;

import dev.mockboard.Constants;
import dev.mockboard.common.domain.RequestMetadata;
import dev.mockboard.common.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestMetadataValidator {

    private final ObjectMapper objectMapper;

    public RequestMetadata validateAndGet(String apiKey, HttpServletRequest request) {
        if (!Constants.VALID_HTTP_METHODS.contains(request.getMethod())) {
            throw new IllegalArgumentException("Unsupported HTTP method: " + request.getMethod());
        }

        if (request.getRequestURI().length() > Constants.MAX_PATH_LENGTH) {
            throw new IllegalArgumentException("Allowed path length exceeded");
        }
        if (StringUtils.hasLength(request.getQueryString()) && request.getQueryString().length() > Constants.MAX_QUERY_STRING_LENGTH) {
            throw new IllegalArgumentException("Allowed query string length exceeded");
        }
        var body = extractAndValidateBody(request);
        if (!body.isBlank() && !isValidJson(body)) {
            throw new IllegalArgumentException("Invalid JSON payload");
        }
        var headers = extractHeaders(request);
        return new RequestMetadata(
                request.getMethod(),
                request.getRequestURI(),
                RequestUtils.extractMockPath(apiKey, request),
                request.getRequestURL().toString(),
                request.getQueryString(),
                serializeHeaders(headers),
                body,
                request.getContentType()
        );
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        var headers = new HashMap<String, String>();
        var names = request.getHeaderNames();
        int count = 0;
        while (names.hasMoreElements() && count < Constants.MAX_WEBHOOK_HEADERS_SIZE) {
            var name = names.nextElement();
            var value = request.getHeader(name);
            if (name.length() < Constants.MAX_HEADER_KEY_LENGTH &&
                    (value != null && value.length() <= Constants.MAX_HEADER_VALUE_LENGTH)) {
                headers.put(name, value);
                count++;
            }
        }
        return headers;
    }

    private String extractAndValidateBody(HttpServletRequest request) {
        if (request.getContentLengthLong() > Constants.MAX_BODY_LENGTH) {
            throw new IllegalArgumentException("Payload too large");
        }

        try {
            char[] buffer = new char[Constants.MAX_BODY_LENGTH + 1];
            int totalRead = 0;
            try (var reader = request.getReader()) {
                int read;
                while ((read = reader.read(buffer, totalRead, buffer.length - totalRead)) != -1) {
                    totalRead += read;
                    if (totalRead > Constants.MAX_BODY_LENGTH) {
                        throw new IllegalArgumentException("Payload exceeds maximum size");
                    }
                }
            }
            return totalRead == 0 ? "" : new  String(buffer, 0, totalRead);
        } catch (Exception e) {
            throw new IllegalArgumentException("Read failure", e);
        }
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (Exception e) {
            log.error("Provided invalid json", e);
            return false;
        }
    }

    private String serializeHeaders(Map<String, String> headers) {
        try {
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            log.error("Unable to serialize headers", e);
            return "{}";
        }
    }
}
