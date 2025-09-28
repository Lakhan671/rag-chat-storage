package com.ragchat.rag_chat_storage.repository;


import com.ragchat.rag_chat_storage.entity.ChatSession;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ChatSessionRepository extends ReactiveCrudRepository<ChatSession, UUID> {

    Flux<ChatSession> findByUserIdOrderByUpdatedAtDesc(String userId);

    Flux<ChatSession> findByUserIdOrderByUpdatedAtDesc(String userId, Pageable pageable);

    Flux<ChatSession> findByUserIdAndIsFavoriteTrueOrderByUpdatedAtDesc(String userId);

    Mono<Long> countByUserId(String userId);

    @Query("SELECT s.*, COUNT(m.id) as message_count " +
            "FROM chat_sessions s " +
            "LEFT JOIN chat_messages m ON s.id = m.session_id " +
            "WHERE s.user_id = :userId " +
            "GROUP BY s.id " +
            "ORDER BY s.updated_at DESC")
    Flux<Object[]> findSessionsWithMessageCount(String userId);
}
