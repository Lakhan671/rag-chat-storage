package com.ragchat.rag_chat_storage.config;



import com.ragchat.rag_chat_storage.exception.RateLimitExceededException;
import com.ragchat.rag_chat_storage.exception.UnauthorizedException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(-1)
@RequiredArgsConstructor
public class SecurityFilter implements WebFilter {

    @Value("${app.security.api-key}")
    private String apiKey;

    private final RateLimitingService rateLimitingService;

    private static final String API_KEY_HEADER = "X-API-Key";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip security for health checks, swagger docs, and actuator endpoints
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Validate API Key
        String providedApiKey = exchange.getRequest().getHeaders().getFirst(API_KEY_HEADER);
        if (providedApiKey == null || !apiKey.equals(providedApiKey)) {
            log.warn("Unauthorized access attempt to: {}", path);
            return Mono.error(new UnauthorizedException("Invalid or missing API key"));
        }

        // Apply rate limiting
        String clientId = getClientIdentifier(exchange);
        Bucket bucket = rateLimitingService.resolveBucket(clientId);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            log.warn("Rate limit exceeded for client: {}", clientId);
            return Mono.error(new RateLimitExceededException("Rate limit exceeded. Try again later."));
        }

        // Add rate limit headers
        exchange.getResponse().getHeaders().add("X-Rate-Limit-Remaining",
                String.valueOf(probe.getRemainingTokens()));

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/actuator") ||
                path.startsWith("/api/v1/health") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api-docs") ||
                path.startsWith("/webjars") ||
                path.equals("/") ||
                path.startsWith("/h2-console");
    }

    private String getClientIdentifier(ServerWebExchange exchange) {
        // Use IP address as client identifier (in production, consider using user ID)
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}
