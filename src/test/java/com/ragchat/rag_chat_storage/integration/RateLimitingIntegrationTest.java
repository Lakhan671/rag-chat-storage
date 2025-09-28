package com.ragchat.rag_chat_storage.integration;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class RateLimitingIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static final String API_KEY = "test-api-key";
    private static final String API_KEY_HEADER = "X-API-Key";

    @Test
    void shouldApplyRateLimiting() {
        // Set a very low rate limit for testing
        System.setProperty("app.rate-limiting.requests-per-minute", "2");
        System.setProperty("app.rate-limiting.bucket-capacity", "2");
        System.setProperty("app.security.api-key", API_KEY);

        // First request should succeed
        webTestClient.get()
                .uri("/api/v1/health/ping")
                .header(API_KEY_HEADER, API_KEY)
                .exchange()
                .expectStatus().isOk();

        // Second request should succeed
        webTestClient.get()
                .uri("/api/v1/health/ping")
                .header(API_KEY_HEADER, API_KEY)
                .exchange()
                .expectStatus().isOk();

        // Third request should be rate limited
        webTestClient.get()
                .uri("/api/v1/health/ping")
                .header(API_KEY_HEADER, API_KEY)
                .exchange()
                .expectStatus().is4xxClientError(); // Rate limit exceeded
    }
}