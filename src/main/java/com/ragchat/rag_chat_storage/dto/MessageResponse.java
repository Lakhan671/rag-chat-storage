package com.ragchat.rag_chat_storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private UUID id;
    private UUID sessionId;
    private String sender;
    private String content;
    private String context;
    private LocalDateTime createdAt;
}