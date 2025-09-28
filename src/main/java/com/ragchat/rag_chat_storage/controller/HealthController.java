package com.ragchat.rag_chat_storage.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "Application health status")
public class HealthController {

    @Operation(summary = "Get application health status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Health status retrieved successfully")
    })
    @GetMapping
    public Mono<Map<String, Object>> health() {
        return Mono.just(Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "rag-chat-storage",
                "version", "1.0.0"
        ));
    }

    @Operation(summary = "Simple ping endpoint")
    @GetMapping("/ping")
    public Mono<Map<String, String>> ping() {
        return Mono.just(Map.of("message", "pong"));
    }
/*
    @GetMapping("/test-db")
    public Mono<String> testDatabase() {
        return r2dbcEntityTemplate.getDatabaseClient()
                .sql("SELECT 1")
                .fetch()
                .first()
                .map(result -> "Database connection successful")
                .onErrorReturn("Database connection failed");
    }*/
}
