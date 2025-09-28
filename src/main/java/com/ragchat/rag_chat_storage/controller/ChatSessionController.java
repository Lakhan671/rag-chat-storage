package com.ragchat.rag_chat_storage.controller;


import com.ragchat.rag_chat_storage.dto.CreateSessionRequest;
import com.ragchat.rag_chat_storage.dto.PagedResponse;
import com.ragchat.rag_chat_storage.dto.SessionResponse;
import com.ragchat.rag_chat_storage.dto.UpdateSessionRequest;
import com.ragchat.rag_chat_storage.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Validated
@Tag(name = "Chat Sessions", description = "Chat session management operations")
public class ChatSessionController {

    private final ChatSessionService sessionService;

    @Operation(summary = "Create a new chat session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        log.info("POST /api/v1/sessions - Creating session for user: {}", request.getUserId());
        return sessionService.createSession(request);
    }

    @Operation(summary = "Get session by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{sessionId}")
    public Mono<SessionResponse> getSession(
            @Parameter(description = "Session ID") @PathVariable UUID sessionId) {
        log.debug("GET /api/v1/sessions/{} - Fetching session", sessionId);
        return sessionService.getSession(sessionId);
    }

    @Operation(summary = "Get user sessions with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public Mono<PagedResponse<SessionResponse>> getUserSessions(
            @Parameter(description = "User ID") @RequestParam String userId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        log.debug("GET /api/v1/sessions - Fetching sessions for user: {}, page: {}, size: {}", userId, page, size);
        return sessionService.getUserSessionsPaginated(userId, page, size);
    }

    @Operation(summary = "Update session (rename or toggle favorite)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session updated successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/{sessionId}")
    public Mono<SessionResponse> updateSession(
            @Parameter(description = "Session ID") @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateSessionRequest request) {
        log.info("PUT /api/v1/sessions/{} - Updating session", sessionId);
        return sessionService.updateSession(sessionId, request);
    }

    @Operation(summary = "Delete session and all its messages")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteSession(
            @Parameter(description = "Session ID") @PathVariable UUID sessionId) {
        log.info("DELETE /api/v1/sessions/{} - Deleting session", sessionId);
        return sessionService.deleteSession(sessionId);
    }
}
