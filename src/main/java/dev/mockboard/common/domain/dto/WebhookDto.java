package dev.mockboard.common.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore private String boardId;
    private String method;
    private String path;
    private String fullUrl;
    private String queryParams;
    private String headers;
    private String body;
    private String contentType;
    private Integer statusCode;
    private Boolean matched;
    private Instant timestamp;
    private Long processingTimeMs;
}
