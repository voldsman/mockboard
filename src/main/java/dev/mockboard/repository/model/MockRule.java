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
public class MockRule implements Serializable {

    private String id;
    private String boardId;
    private String method;
    private String path;
    private String headers;
    private String body;
    private int statusCode;
    private int delay; //ms
    private Instant timestamp;
}
