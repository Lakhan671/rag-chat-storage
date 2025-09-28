package com.ragchat.rag_chat_storage.exception;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
public class ErrorResponse {
    private String error;
    private String message;
    private int status;
    private String path;
    private LocalDateTime timestamp;
    private List<ValidationError> validationErrors;

    @Data
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}