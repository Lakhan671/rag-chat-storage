package com.ragchat.rag_chat_storage.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResourceNotFound(
            ResourceNotFoundException ex, ServerWebExchange exchange) {
        log.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .status(HttpStatus.NOT_FOUND.value())
                .path(exchange.getRequest().getPath().value())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnauthorized(
            UnauthorizedException ex, ServerWebExchange exchange) {
        log.warn("Unauthorized access: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("UNAUTHORIZED")
                .message(ex.getMessage())
                .status(HttpStatus.UNAUTHORIZED.value())
                .path(exchange.getRequest().getPath().value())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse));
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleRateLimitExceeded(
            RateLimitExceededException ex, ServerWebExchange exchange) {
        log.warn("Rate limit exceeded: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("RATE_LIMIT_EXCEEDED")
                .message(ex.getMessage())
                .status(HttpStatus.TOO_MANY_REQUESTS.value())
                .path(exchange.getRequest().getPath().value())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(errorResponse));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationErrors(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        log.warn("Validation errors: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("VALIDATION_ERROR")
                .message("Request validation failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(exchange.getRequest().getPath().value())
                .timestamp(LocalDateTime.now())
                .validationErrors(validationErrors)
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error occurred", ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .path(exchange.getRequest().getPath().value())
                .timestamp(LocalDateTime.now())
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse));
    }

    private ErrorResponse.ValidationError mapFieldError(FieldError fieldError) {
        return ErrorResponse.ValidationError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }
}