package dev.mockboard.core.config.advice;

import dev.mockboard.core.common.exception.NotFoundException;
import dev.mockboard.core.common.exception.RateLimitExceededException;
import dev.mockboard.core.common.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    private record ExceptionResponse(String error, LocalDateTime timestamp) {}

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex) {
        var exceptionResponse = new ExceptionResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<ExceptionResponse> handleUnauthorizedException(UnauthorizedException ex) {
        var exceptionResponse = new ExceptionResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({RateLimitExceededException.class})
    public ResponseEntity<ExceptionResponse> handleRateLimitExceededException(RateLimitExceededException ex) {
        var exceptionResponse = new ExceptionResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ExceptionResponse> handleException(Exception ex) {
        var exceptionResponse = new ExceptionResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
