package com.ragchat.rag_chat_storage.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitingService {

    @Value("${app.rate-limiting.requests-per-minute:60}")
    private int requestsPerMinute;

    @Value("${app.rate-limiting.bucket-capacity:100}")
    private int bucketCapacity;

    private final ConcurrentHashMap<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, this::newBucket);
    }

    private Bucket newBucket(String key) {
        Bandwidth limit = Bandwidth.classic(bucketCapacity,
                Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
