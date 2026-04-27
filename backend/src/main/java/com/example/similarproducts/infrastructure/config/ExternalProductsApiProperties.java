package com.example.similarproducts.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Typed configuration for the external product API adapter.
 */
@ConfigurationProperties(prefix = "external-products-api")
public record ExternalProductsApiProperties(
        String baseUrl,
        int connectTimeoutMs,
        int responseTimeoutMs,
        int maxConnections,
        int pendingAcquireMaxCount,
        Duration cacheTtl,
        int detailConcurrency
) {
}
