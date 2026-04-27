package com.example.similarproducts.interfaces.rest;

import com.example.similarproducts.application.usecase.GetSimilarProductsUseCase;
import com.example.similarproducts.domain.model.Product;
import com.example.similarproducts.infrastructure.http.client.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

/**
 * WebFlux tests for the similar-products REST controller.
 */
@WebFluxTest(SimilarProductsController.class)
@Import(ApiExceptionHandler.class)
class SimilarProductsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private StubSimilarProductsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase.result = Mono.just(List.of());
    }

    @Test
    void shouldReturnSimilarProducts() {
        useCase.result = Mono.just(List.of(
                new Product("2", "Dress", BigDecimal.valueOf(19.99), true)
        ));

        webTestClient.get()
                .uri("/product/1/similar")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("2")
                .jsonPath("$[0].name").isEqualTo("Dress")
                .jsonPath("$[0].availability").isEqualTo(true);
    }

    @Test
    void shouldReturnNotFoundWhenMainProductDoesNotExist() {
        useCase.result = Mono.error(new ProductNotFoundException("404"));

        webTestClient.get()
                .uri("/product/404/similar")
                .exchange()
                .expectStatus().isNotFound();
    }

    /**
     * Test-only bean configuration for replacing the use case with a controllable stub.
     */
    @TestConfiguration
    static class TestConfig {

        @Bean
        StubSimilarProductsUseCase stubSimilarProductsUseCase() {
            return new StubSimilarProductsUseCase();
        }
    }

    /**
     * Stub use case that lets each test define the controller response.
     */
    static class StubSimilarProductsUseCase extends GetSimilarProductsUseCase {

        private Mono<List<Product>> result = Mono.just(List.of());

        StubSimilarProductsUseCase() {
            super(null, 1);
        }

        @Override
        public Mono<List<Product>> execute(String productId) {
            return result;
        }
    }
}
