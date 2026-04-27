package com.example.similarproducts.infrastructure.http.client;

import com.example.similarproducts.domain.model.Product;
import com.example.similarproducts.infrastructure.config.ExternalProductsApiProperties;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Adapter tests for the external product API client.
 */
class ProductApiClientTest {

    private MockWebServer server;
    private ProductApiClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        client = createClient(Duration.ofSeconds(5));
    }

    private ProductApiClient createClient(Duration timeout) {
        ExternalProductsApiProperties properties = new ExternalProductsApiProperties(
                server.url("/").toString(),
                100,
                Math.toIntExact(timeout.toMillis()),
                16,
                32,
                Duration.ofMinutes(1),
                8
        );
        return new ProductApiClient(WebClient.builder().baseUrl(properties.baseUrl()).build(), properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void shouldFetchSimilarProductIds() {
        server.enqueue(jsonResponse("[\"2\",\"3\",\"4\"]"));

        StepVerifier.create(client.findSimilarProductIds("1"))
                .expectNext(List.of("2", "3", "4"))
                .verifyComplete();
    }

    @Test
    void shouldMapMissingMainProductToNotFound() {
        server.enqueue(emptyResponse(404));

        StepVerifier.create(client.findSimilarProductIds("404"))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void shouldMapSimilarIdsDownstreamFailuresToExternalApiException() {
        server.enqueue(emptyResponse(500));

        StepVerifier.create(client.findSimilarProductIds("1"))
                .expectError(ExternalProductApiException.class)
                .verify();
    }

    @Test
    void shouldCacheSuccessfulSimilarIdsResponses() {
        server.enqueue(jsonResponse("[\"2\",\"3\"]"));

        StepVerifier.create(client.findSimilarProductIds("1"))
                .expectNext(List.of("2", "3"))
                .verifyComplete();
        StepVerifier.create(client.findSimilarProductIds("1"))
                .expectNext(List.of("2", "3"))
                .verifyComplete();

        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    @Test
    void shouldFetchProductDetail() {
        server.enqueue(jsonResponse("""
                {"id":"2","name":"Dress","price":19.99,"availability":true}
                """));

        StepVerifier.create(client.findProductById("2"))
                .expectNext(new Product("2", "Dress", BigDecimal.valueOf(19.99), true))
                .verifyComplete();
    }

    @Test
    void shouldIgnoreMissingProductDetail() {
        server.enqueue(emptyResponse(404));

        StepVerifier.create(client.findProductById("5"))
                .verifyComplete();
    }

    @Test
    void shouldIgnoreProductDetailServerErrors() {
        server.enqueue(emptyResponse(500));

        StepVerifier.create(client.findProductById("6"))
                .verifyComplete();
    }

    @Test
    void shouldIgnoreProductDetailTimeouts() {
        client = createClient(Duration.ofMillis(200));
        server.enqueue(jsonResponse("""
                {"id":"1000","name":"Slow","price":1,"availability":true}
                """).setBodyDelay(500, TimeUnit.MILLISECONDS));

        StepVerifier.create(client.findProductById("1000"))
                .verifyComplete();
    }

    @Test
    void shouldCacheUnavailableProductDetails() {
        server.enqueue(emptyResponse(404));

        StepVerifier.create(client.findProductById("5"))
                .verifyComplete();
        StepVerifier.create(client.findProductById("5"))
                .verifyComplete();

        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    private MockResponse jsonResponse(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
    }

    private MockResponse emptyResponse(int statusCode) {
        return new MockResponse()
                .setResponseCode(statusCode)
                .setHeader("Content-Length", "0")
                .setBody("");
    }
}
