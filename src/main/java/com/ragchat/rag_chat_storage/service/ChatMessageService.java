package com.ragchat.rag_chat_storage.service;


import com.ragchat.rag_chat_storage.dto.CreateMessageRequest;
import com.ragchat.rag_chat_storage.dto.MessageResponse;
import com.ragchat.rag_chat_storage.dto.PagedResponse;
import com.ragchat.rag_chat_storage.entity.ChatMessage;
import com.ragchat.rag_chat_storage.exception.ResourceNotFoundException;
import com.ragchat.rag_chat_storage.repository.ChatMessageRepository;
import com.ragchat.rag_chat_storage.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;

    public Mono<MessageResponse> createMessage(UUID sessionId, CreateMessageRequest request) {
        log.info("Creating new message for session: {}", sessionId);

        return sessionRepository.existsById(sessionId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Session not found with ID: " + sessionId));
                    }

                    ChatMessage message = ChatMessage.builder()
                            .id(UUID.randomUUID())
                            .sessionId(sessionId)
                            .sender(request.getSender())
                            .content(request.getContent())
                            .context(request.getContext())
                            .createdAt(LocalDateTime.now())
                            .build();

                    return messageRepository.save(message)
                            .doOnSuccess(saved -> log.info("Created message with ID: {}", saved.getId()))
                            .map(this::toMessageResponse);
                });
    }

    public Flux<MessageResponse> getSessionMessages(UUID sessionId, int page, int size) {
        log.debug("Fetching messages for session: {}, page: {}, size: {}", sessionId, page, size);

        return sessionRepository.existsById(sessionId)
                .flatMapMany(exists -> {
                    if (!exists) {
                        return Flux.error(new ResourceNotFoundException("Session not found with ID: " + sessionId));
                    }

                    if (page >= 0 && size > 0) {
                        PageRequest pageRequest = PageRequest.of(page, size);
                        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId, pageRequest);
                    } else {
                        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
                    }
                })
                .map(this::toMessageResponse);
    }

    public Mono<PagedResponse<MessageResponse>> getSessionMessagesPaginated(UUID sessionId, int page, int size) {
        log.debug("Fetching paginated messages for session: {}", sessionId);

        return sessionRepository.existsById(sessionId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new ResourceNotFoundException("Session not found with ID: " + sessionId));
                    }

                    return messageRepository.countBySessionId(sessionId)
                            .flatMap(totalElements -> {
                                int totalPages = (int) Math.ceil((double) totalElements / size);

                                return getSessionMessages(sessionId, page, size)
                                        .collectList()
                                        .map(content -> PagedResponse.<MessageResponse>builder()
                                                .content(content)
                                                .page(page)
                                                .size(size)
                                                .totalElements(totalElements)
                                                .totalPages(totalPages)
                                                .first(page == 0)
                                                .last(page >= totalPages - 1)
                                                .build());
                            });
                });
    }

    private MessageResponse toMessageResponse(ChatMessage message) {
        return MessageResponse.builder()
                .id(message.getId())
                .sessionId(message.getSessionId())
                .sender(message.getSender())
                .content(message.getContent())
                .context(message.getContext())
                .createdAt(message.getCreatedAt())
                .build();
    }
}