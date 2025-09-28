package com.ragchat.rag_chat_storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// Create Session Request
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionRequest {
    @NotBlank(message = "User ID is required")
    @Size(max = 100, message = "User ID must not exceed 100 characters")
    private String userId;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;
}

