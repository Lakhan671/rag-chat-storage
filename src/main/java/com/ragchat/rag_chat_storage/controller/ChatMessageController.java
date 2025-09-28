package com.ragchat.rag_chat_storage.controller;



import com.ragchat.rag_chat_storage.dto.CreateMessageRequest;
import com.ragchat.rag_chat_storage.dto.MessageResponse;
import com.ragchat.rag_chat_storage.dto.PagedResponse;
import com.ragchat.rag_chat_storage.service.ChatMessageService;
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
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@RequiredArgsConstructor
@Validated
@Tag(name = "Chat Messages", description = "Chat message operations")
public class ChatMessageController {

    private final ChatMessageService messageService;

    @Operation(summary = "Add a message to a session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Message created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MessageResponse> createMessage(
            @Parameter(description = "Session ID") @PathVariable UUID sessionId,
            @Valid @RequestBody CreateMessageRequest request) {
        log.info("POST /api/v1/sessions/{}/messages - Creating message", sessionId);
        return messageService.createMessage(sessionId, request);
    }

    @Operation(summary = "Get messages for a session with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Messages retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Session not found"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public Mono<PagedResponse<MessageResponse>> getSessionMessages(
            @Parameter(description = "Session ID") @PathVariable UUID sessionId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") @Min(1) @Max(200) int size) {
        log.debug("GET /api/v1/sessions/{}/messages - Fetching messages, page: {}, size: {}", sessionId, page, size);
        return messageService.getSessionMessagesPaginated(sessionId, page, size);
    }
}
