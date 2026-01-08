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
public class MockRuleDto implements Serializable {

    private String id;
    private String boardId;
    @JsonIgnore private String apiKey;
    private String method;
    private String path;
    private String headers;
    private String body;
    private int statusCode;
    private Instant timestamp;
}
