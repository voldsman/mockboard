package dev.mockboard.repository.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board implements Serializable {

    private String id; // id and apiKey
    private String ownerToken;
    private Instant timestamp;
    private Map<BoardStatType, Long> stats;
}
