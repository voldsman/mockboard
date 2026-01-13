package dev.mockboard.common.domain.dto;

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
public class WebhookDto implements Serializable {
    private String id;
    private String boardId;
    private String method;
    private String path;
    private String fullUrl;
    private String queryParams;
    private String headers;
    private String body;
    private String contentType;
    private int statusCode;
    private boolean matched;
    private Instant timestamp;
    private long processingTimeMs;
}
