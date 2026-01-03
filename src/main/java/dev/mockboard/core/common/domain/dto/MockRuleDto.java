package dev.mockboard.core.common.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockRuleDto {

    private String id;
    private String boardId;
    private String apiKey;
    private String method;
    private String path;
    private Map<String, String> headers;
    private String body;
    private int statusCode;
    private LocalDateTime createdAt;
}
