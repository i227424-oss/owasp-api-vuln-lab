package edu.nu.owaspapivulnlab.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIX-5: Rate limiting configuration using Bucket4j
 * Limits sensitive operations like money transfers
 */
@Configuration
public class RateLimitConfig {

    /**
     * In-memory bucket storage per user
     * In production, use Redis or similar distributed cache
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Bean
    public Map<String, Bucket> rateLimitBuckets() {
        return buckets;
    }

    /**
     * Creates a bucket with rate limit: 5 requests per minute
     */
    public Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k -> createBucket());
    }

    private Bucket createBucket() {
        // Allow 5 transfers per minute
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
