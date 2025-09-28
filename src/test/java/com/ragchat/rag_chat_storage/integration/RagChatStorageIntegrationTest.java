package com.ragchat.rag_chat_storage.integration;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.ragchat.rag_chat_storage.dto.CreateMessageRequest;
import com.ragchat.rag_chat_storage.dto.CreateSessionRequest;
import com.ragchat.rag_chat_storage.dto.SessionResponse;
import com.ragchat.rag_chat_storage.dto.UpdateSessionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class RagChatStorageIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String API_KEY = "test-api-key";
    private static final String API_KEY_HEADER = "X-API-Key";

    @BeforeEach
    void setUp() {
        // Set the test API key
        System.setProperty("app.security.api-key", API_KEY);
    }

    @Test
    void shouldCreateSessionAndAddMessages() {
        // Create session
        CreateSessionRequest createSessionRequest = CreateSessionRequest.builder()
                .userId("test-user")
                .title("Integration Test Session")
                .build();

        SessionResponse sessionResponse = webTestClient.post()
                .uri("/api/v1/sessions")
                .header(API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createSessionRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SessionResponse.class)
                .returnResult()
                .getResponseBody();

        assert sessionResponse != null;
        UUID sessionId = sessionResponse.getId();

        // Add user message
        CreateMessageRequest userMessage = CreateMessageRequest.builder()
                .sender("USER")
                .content("Hello, what is artificial intelligence?")
                .build();

        webTestClient.post()
                .uri("/api/v1/sessions/{sessionId}/messages", sessionId)
                .header(API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userMessage)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.sender").isEqualTo("USER")
                .jsonPath("$.content").isEqualTo("Hello, what is artificial intelligence?");

        // Add assistant message
        CreateMessageRequest assistantMessage = CreateMessageRequest.builder()
                .sender("ASSISTANT")
                .content("Artificial Intelligence (AI) is a branch of computer science...")
                .context("Retrieved from knowledge base: AI definitions and explanations")
                .build();

        webTestClient.post()
                .uri("/api/v1/sessions/{sessionId}/messages", sessionId)
                .header(API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(assistantMessage)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.sender").isEqualTo("ASSISTANT")
                .jsonPath("$.context").isEqualTo("Retrieved from knowledge base: AI definitions and explanations");

        // Get session messages
        webTestClient.get()
                .uri("/api/v1/sessions/{sessionId}/messages?page=0&size=10", sessionId)
                .header(API_KEY_HEADER, API_KEY)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.totalElements").isEqualTo(2);

        // Update session
        UpdateSessionRequest updateRequest = UpdateSessionRequest.builder()
                .title("Updated Integration Test Session")
                .isFavorite(true)
                .build();

        webTestClient.put()
                .uri("/api/v1/sessions/{sessionId}", sessionId)
                .header(API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Updated Integration Test Session")
                .jsonPath("$.isFavorite").isEqualTo(true);

        // Get user sessions
        webTestClient.get()
                .uri("/api/v1/sessions?userId=test-user&page=0&size=10")
                .header(API_KEY_HEADER, API_KEY)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].messageCount").isEqualTo(2);

        // Delete session
        webTestClient.delete()
                .uri("/api/v1/sessions/{sessionId}", sessionId)
                .header(API_KEY_HEADER, API_KEY)
                .exchange()
                .expectStatus().isNoContent();

        // Verify session is deleted
        webTestClient.get()
                .uri("/api/v1/sessions/{sessionId}", sessionId)
                .header(API_KEY_HEADER, API_KEY)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturnUnauthorizedWithoutApiKey() {
        CreateSessionRequest request = CreateSessionRequest.builder()
                .userId("test-user")
                .title("Test Session")
                .build();

        webTestClient.post()
                .uri("/api/v1/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturnUnauthorizedWithInvalidApiKey() {
        CreateSessionRequest request = CreateSessionRequest.builder()
                .userId("test-user")
                .title("Test Session")
                .build();

        webTestClient.post()
                .uri("/api/v1/sessions")
                .header(API_KEY_HEADER, "invalid-key")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldValidateRequestBody() {
        CreateSessionRequest invalidRequest = CreateSessionRequest.builder()
                .userId("") // Invalid empty userId
                .build();

        webTestClient.post()
                .uri("/api/v1/sessions")
                .header(API_KEY_HEADER, API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void shouldReturnHealthStatus() {
        webTestClient.get()
                .uri("/api/v1/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP")
                .jsonPath("$.service").isEqualTo("rag-chat-storage");
    }

    @Test
    void shouldReturnPingResponse() {
        webTestClient.get()
                .uri("/api/v1/health/ping")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").isEqualTo("pong");
    }
}

