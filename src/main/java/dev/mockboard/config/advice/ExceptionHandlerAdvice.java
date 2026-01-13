package dev.mockboard.config.advice;

import dev.mockboard.common.exception.BadRequestException;
import dev.mockboard.common.exception.NotFoundException;
import dev.mockboard.common.exception.RateLimitExceededException;
import dev.mockboard.common.exception.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(annotations = RestController.class)
public class ExceptionHandlerAdvice {

    private record ExceptionResponse(String error, LocalDateTime timestamp) {}

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex) {
        var exceptionResponse = new ExceptionResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({BadRequestException.class})
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException ex) {
        var exceptionResponse = new ExceptionResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
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

    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<ExceptionResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        var exceptionResponse = new ExceptionResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MissingRequestHeaderException.class})
    public ResponseEntity<ExceptionResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        var exceptionResponse = new ExceptionResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ExceptionResponse> handleException(Exception ex) {
        // handle SSE edge cases on shutdown/restart
        if (ex.getClass().getSimpleName().contains("AsyncRequestNotUsableException") ||
                ex.getClass().getSimpleName().contains("ClientAbortException")) {
            return null;
        }

        var exceptionResponse = new ExceptionResponse(ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
