package dev.mockboard.common.domain;

import java.time.LocalDateTime;
import java.util.Map;

public record ExceptionResponse(String error,
                                LocalDateTime timestamp,
                                Map<String, String> errors) {

    public ExceptionResponse(String error, LocalDateTime timestamp) {
        this(error, timestamp, null);
    }
}