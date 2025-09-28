package com.ragchat.rag_chat_storage.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_messages")
public class ChatMessage {

    @Id
    private UUID id;

    private UUID sessionId;

    private String sender; // USER or ASSISTANT

    private String content;

    private String context; // Optional RAG context

    @CreatedDate
    private LocalDateTime createdAt;
}