package com.ragchat.rag_chat_storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageRequest {
    @NotBlank(message = "Sender is required")
    @Pattern(regexp = "^(USER|ASSISTANT)$", message = "Sender must be either USER or ASSISTANT")
    private String sender;

    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    @Size(max = 5000, message = "Context must not exceed 5000 characters")
    private String context;
}