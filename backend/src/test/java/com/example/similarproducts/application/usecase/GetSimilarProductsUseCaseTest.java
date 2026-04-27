package com.example.similarproducts.application.usecase;

import com.example.similarproducts.domain.model.Product;
import com.example.similarproducts.domain.port.ProductCatalogPort;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

/**
 * Unit tests for the similar-products use case orchestration rules.
 */
class GetSimilarProductsUseCaseTest {

    @Test
    void shouldReturnSimilarProductsPreservingSimilarityOrder() {
        ProductCatalogPort port = new ProductCatalogPort() {
            @Override
            public Mono<List<String>> findSimilarProductIds(String productId) {
                return Mono.just(List.of("2", "3", "4"));
            }

            @Override
            public Mono<Product> findProductById(String productId) {
                return switch (productId) {
                    case "2" -> Mono.just(new Product("2", "Dress", BigDecimal.valueOf(19.99), true));
                    case "3" -> Mono.just(new Product("3", "Blazer", BigDecimal.valueOf(29.99), false));
                    case "4" -> Mono.just(new Product("4", "Boots", BigDecimal.valueOf(39.99), true));
                    default -> Mono.empty();
                };
            }
        };

        GetSimilarProductsUseCase useCase = new GetSimilarProductsUseCase(port, 8);

        StepVerifier.create(useCase.execute("1"))
                .expectNextMatches(products -> products.size() == 3
                        && products.get(0).id().equals("2")
                        && products.get(1).id().equals("3")
                        && products.get(2).id().equals("4"))
                .verifyComplete();
    }

    @Test
    void shouldIgnoreUnavailableProductDetails() {
        ProductCatalogPort port = new ProductCatalogPort() {
            @Override
            public Mono<List<String>> findSimilarProductIds(String productId) {
                return Mono.just(List.of("1", "2", "5"));
            }

            @Override
            public Mono<Product> findProductById(String productId) {
                if (productId.equals("5")) {
                    return Mono.empty();
                }
                return Mono.just(new Product(productId, "Product " + productId, BigDecimal.TEN, true));
            }
        };

        GetSimilarProductsUseCase useCase = new GetSimilarProductsUseCase(port, 8);

        StepVerifier.create(useCase.execute("4"))
                .expectNextMatches(products -> products.size() == 2
                        && products.get(0).id().equals("1")
                        && products.get(1).id().equals("2"))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoSimilarProducts() {
        ProductCatalogPort port = new ProductCatalogPort() {
            @Override
            public Mono<List<String>> findSimilarProductIds(String productId) {
                return Mono.just(List.of());
            }

            @Override
            public Mono<Product> findProductById(String productId) {
                return Mono.error(new AssertionError("No detail calls expected"));
            }
        };

        GetSimilarProductsUseCase useCase = new GetSimilarProductsUseCase(port, 8);

        StepVerifier.create(useCase.execute("1"))
                .expectNext(List.of())
                .verifyComplete();
    }

    @Test
    void shouldRemoveDuplicateSimilarIds() {
        ProductCatalogPort port = new ProductCatalogPort() {
            @Override
            public Mono<List<String>> findSimilarProductIds(String productId) {
                return Mono.just(List.of("2", "2", "3"));
            }

            @Override
            public Mono<Product> findProductById(String productId) {
                return Mono.just(new Product(productId, "Product " + productId, BigDecimal.TEN, true));
            }
        };

        GetSimilarProductsUseCase useCase = new GetSimilarProductsUseCase(port, 8);

        StepVerifier.create(useCase.execute("1"))
                .expectNextMatches(products -> products.size() == 2
                        && products.get(0).id().equals("2")
                        && products.get(1).id().equals("3"))
                .verifyComplete();
    }
}
