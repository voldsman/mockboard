package dev.mockboard.common.domain;

public record RequestMetadata(
        String method,
        String path,
        String mockPath,
        String fullUrl,
        String queryParams,
        String headers,
        String requestBody,
        String contentType
) {}
