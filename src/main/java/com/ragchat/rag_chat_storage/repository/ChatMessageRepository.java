package com.ragchat.rag_chat_storage.repository;


import com.ragchat.rag_chat_storage.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ChatMessageRepository extends ReactiveCrudRepository<ChatMessage, UUID> {

    Flux<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    Flux<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId, Pageable pageable);

    Mono<Long> countBySessionId(UUID sessionId);

    Mono<Void> deleteBySessionId(UUID sessionId);
}