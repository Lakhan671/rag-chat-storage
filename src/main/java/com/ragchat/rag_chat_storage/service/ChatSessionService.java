package com.ragchat.rag_chat_storage.service;

import com.ragchat.rag_chat_storage.dto.CreateSessionRequest;
import com.ragchat.rag_chat_storage.dto.PagedResponse;
import com.ragchat.rag_chat_storage.dto.SessionResponse;
import com.ragchat.rag_chat_storage.dto.UpdateSessionRequest;
import com.ragchat.rag_chat_storage.entity.ChatSession;
import com.ragchat.rag_chat_storage.exception.ResourceNotFoundException;
import com.ragchat.rag_chat_storage.repository.ChatMessageRepository;
import com.ragchat.rag_chat_storage.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final R2dbcEntityTemplate template;

    public Mono<SessionResponse> createSession(CreateSessionRequest request) {
        log.info("Creating new chat session for user: {}", request.getUserId());

        ChatSession session = ChatSession.builder()
                .id(UUID.randomUUID())
                .userId(request.getUserId())
                .title(request.getTitle() != null ? request.getTitle() : "New Chat")
                .isFavorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return template.insert(ChatSession.class).using(session)  // forces INSERT
                .doOnSuccess(saved -> log.info("Created session with ID: {}", saved.getId()))
                .map(this::toSessionResponse);
    }

    public Mono<SessionResponse> getSession(UUID sessionId) {
        log.debug("Fetching session: {}", sessionId);

        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Session not found with ID: " + sessionId)))
                .flatMap(session ->
                        messageRepository.countBySessionId(sessionId)
                                .map(count -> toSessionResponse(session, count))
                );
    }

    public Flux<SessionResponse> getUserSessions(String userId, int page, int size) {
        log.debug("Fetching sessions for user: {}, page: {}, size: {}", userId, page, size);

        PageRequest pageRequest = PageRequest.of(page, size);

        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageRequest)
                .flatMap(session ->
                        messageRepository.countBySessionId(session.getId())
                                .map(count -> toSessionResponse(session, count))
                );
    }

    public Mono<SessionResponse> updateSession(UUID sessionId, UpdateSessionRequest request) {
        log.info("Updating session: {}", sessionId);

        return sessionRepository.findById(sessionId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Session not found with ID: " + sessionId)))
                .flatMap(session -> {
                    if (request.getTitle() != null) {
                        session.setTitle(request.getTitle());
                    }
                    if (request.getIsFavorite() != null) {
                        session.setIsFavorite(request.getIsFavorite());
                    }
                    session.setUpdatedAt(LocalDateTime.now());

                    return sessionRepository.save(session);
                })
                .doOnSuccess(updated -> log.info("Updated session: {}", sessionId))
                .map(this::toSessionResponse);
    }

    @Transactional
    public Mono<Void> deleteSession(UUID sessionId) {
        log.info("Deleting session: {}", sessionId);

        return sessionRepository.existsById(sessionId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Session not found with ID: " + sessionId));
                    }

                    return messageRepository.deleteBySessionId(sessionId)
                            .then(sessionRepository.deleteById(sessionId))
                            .doOnSuccess(v -> log.info("Deleted session and messages: {}", sessionId));
                });
    }

    public Mono<PagedResponse<SessionResponse>> getUserSessionsPaginated(String userId, int page, int size) {
        log.debug("Fetching paginated sessions for user: {}", userId);

        return sessionRepository.countByUserId(userId)
                .flatMap(totalElements -> {
                    int totalPages = (int) Math.ceil((double) totalElements / size);

                    return getUserSessions(userId, page, size)
                            .collectList()
                            .map(content -> PagedResponse.<SessionResponse>builder()
                                    .content(content)
                                    .page(page)
                                    .size(size)
                                    .totalElements(totalElements)
                                    .totalPages(totalPages)
                                    .first(page == 0)
                                    .last(page >= totalPages - 1)
                                    .build());
                });
    }

    private SessionResponse toSessionResponse(ChatSession session) {
        return toSessionResponse(session, 0L);
    }

    private SessionResponse toSessionResponse(ChatSession session, Long messageCount) {
        return SessionResponse.builder()
                .id(session.getId())
                .userId(session.getUserId())
                .title(session.getTitle())
                .isFavorite(session.getIsFavorite())
                .createdAt(session.getCreatedAt())
                .updatedAt(session.getUpdatedAt())
                .messageCount(messageCount)
                .build();
    }
}