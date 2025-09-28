package com.ragchat.rag_chat_storage.service;



import com.ragchat.rag_chat_storage.dto.CreateMessageRequest;
import com.ragchat.rag_chat_storage.entity.ChatMessage;
import com.ragchat.rag_chat_storage.exception.ResourceNotFoundException;
import com.ragchat.rag_chat_storage.repository.ChatMessageRepository;
import com.ragchat.rag_chat_storage.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private ChatMessageRepository messageRepository;

    @Mock
    private ChatSessionRepository sessionRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private UUID sessionId;
    private UUID messageId;
    private ChatMessage testMessage;
    private CreateMessageRequest createRequest;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        messageId = UUID.randomUUID();

        testMessage = ChatMessage.builder()
                .id(messageId)
                .sessionId(sessionId)
                .sender("USER")
                .content("Test message content")
                .context("Test context")
                .createdAt(LocalDateTime.now())
                .build();

        createRequest = CreateMessageRequest.builder()
                .sender("USER")
                .content("Test message content")
                .context("Test context")
                .build();
    }

    @Test
    void createMessage_ShouldCreateMessageSuccessfully() {
        // Given
        when(sessionRepository.existsById(sessionId)).thenReturn(Mono.just(true));
        when(messageRepository.save(any(ChatMessage.class))).thenReturn(Mono.just(testMessage));

        // When & Then
        StepVerifier.create(chatMessageService.createMessage(sessionId, createRequest))
                .expectNextMatches(response ->
                        response.getSessionId().equals(sessionId) &&
                                response.getSender().equals("USER") &&
                                response.getContent().equals("Test message content"))
                .verifyComplete();
    }

    @Test
    void createMessage_ShouldThrowException_WhenSessionNotExists() {
        // Given
        when(sessionRepository.existsById(sessionId)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(chatMessageService.createMessage(sessionId, createRequest))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void getSessionMessages_ShouldReturnMessages() {
        // Given
        when(sessionRepository.existsById(sessionId)).thenReturn(Mono.just(true));
        when(messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId))
                .thenReturn(Flux.just(testMessage));

        // When & Then
        StepVerifier.create(chatMessageService.getSessionMessages(sessionId, -1, -1))
                .expectNextMatches(response ->
                        response.getSessionId().equals(sessionId) &&
                                response.getSender().equals("USER"))
                .verifyComplete();
    }
}