package com.ragchat.rag_chat_storage.service;


import com.ragchat.rag_chat_storage.dto.CreateSessionRequest;
import com.ragchat.rag_chat_storage.dto.UpdateSessionRequest;
import com.ragchat.rag_chat_storage.entity.ChatSession;
import com.ragchat.rag_chat_storage.exception.ResourceNotFoundException;
import com.ragchat.rag_chat_storage.repository.ChatMessageRepository;
import com.ragchat.rag_chat_storage.repository.ChatSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatSessionServiceTest {

    @Mock
    private ChatSessionRepository sessionRepository;

    @Mock
    private ChatMessageRepository messageRepository;

    @InjectMocks
    private ChatSessionService chatSessionService;

    private UUID sessionId;
    private ChatSession testSession;
    private CreateSessionRequest createRequest;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        testSession = ChatSession.builder()
                .id(sessionId)
                .userId("test-user")
                .title("Test Session")
                .isFavorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateSessionRequest.builder()
                .userId("test-user")
                .title("Test Session")
                .build();
    }

    @Test
    void createSession_ShouldCreateSessionSuccessfully() {
        // Given
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(Mono.just(testSession));

        // When & Then
        StepVerifier.create(chatSessionService.createSession(createRequest))
                .expectNextMatches(response ->
                        response.getUserId().equals("test-user") &&
                                response.getTitle().equals("Test Session") &&
                                !response.getIsFavorite())
                .verifyComplete();
    }

    @Test
    void getSession_ShouldReturnSession_WhenExists() {
        // Given
        when(sessionRepository.findById(sessionId)).thenReturn(Mono.just(testSession));
        when(messageRepository.countBySessionId(sessionId)).thenReturn(Mono.just(5L));

        // When & Then
        StepVerifier.create(chatSessionService.getSession(sessionId))
                .expectNextMatches(response ->
                        response.getId().equals(sessionId) &&
                                response.getMessageCount().equals(5L))
                .verifyComplete();
    }

    @Test
    void getSession_ShouldThrowException_WhenNotExists() {
        // Given
        when(sessionRepository.findById(sessionId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(chatSessionService.getSession(sessionId))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void updateSession_ShouldUpdateTitle() {
        // Given
        UpdateSessionRequest updateRequest = UpdateSessionRequest.builder()
                .title("Updated Title")
                .isFavorite(true)
                .build();

        ChatSession updatedSession = ChatSession.builder()
                .id(sessionId)
                .userId("test-user")
                .title("Updated Title")
                .isFavorite(true)
                .createdAt(testSession.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();

        when(sessionRepository.findById(sessionId)).thenReturn(Mono.just(testSession));
        when(sessionRepository.save(any(ChatSession.class))).thenReturn(Mono.just(updatedSession));

        // When & Then
        StepVerifier.create(chatSessionService.updateSession(sessionId, updateRequest))
                .expectNextMatches(response ->
                        response.getTitle().equals("Updated Title") &&
                                response.getIsFavorite())
                .verifyComplete();
    }

    @Test
    void deleteSession_ShouldDeleteSuccessfully() {
        // Given
        when(sessionRepository.existsById(sessionId)).thenReturn(Mono.just(true));
        when(messageRepository.deleteBySessionId(sessionId)).thenReturn(Mono.empty());
        when(sessionRepository.deleteById(sessionId)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(chatSessionService.deleteSession(sessionId))
                .verifyComplete();
    }

    @Test
    void deleteSession_ShouldThrowException_WhenNotExists() {
        // Given
        when(sessionRepository.existsById(sessionId)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(chatSessionService.deleteSession(sessionId))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }
}
