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
public class BoardDto implements Serializable {

    private String id;
    private String ownerToken;
    private Instant timestamp;
}
