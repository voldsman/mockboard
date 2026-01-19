package dev.mockboard.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Webhook implements Serializable {
    private String id;
    private String boardId;
    private String method;
    private String path;
    private String fullUrl;
    private String queryParams;
    private String headers;
    private String body;
    private String contentType;
    private Integer statusCode;

    // metadata
    private boolean matched;
    private Instant timestamp;
    private long processingTimeMs;
}
