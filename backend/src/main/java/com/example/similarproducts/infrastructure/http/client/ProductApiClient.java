package com.example.similarproducts.infrastructure.http.client;

import com.example.similarproducts.domain.model.Product;
import com.example.similarproducts.domain.port.ProductCatalogPort;
import com.example.similarproducts.infrastructure.config.ExternalProductsApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebClient adapter that implements the product catalog port against the external API.
 */
@Component
public class ProductApiClient implements ProductCatalogPort {

    private static final Logger log = LoggerFactory.getLogger(ProductApiClient.class);
    private static final int HTTP_NOT_FOUND = 404;
    private static final ParameterizedTypeReference<List<String>> STRING_LIST =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient webClient;
    private final Duration timeout;
    private final Duration cacheTtl;
    private final Map<String, Mono<List<String>>> similarIdsCache = new ConcurrentHashMap<>();
    private final Map<String, Mono<Optional<Product>>> productCache = new ConcurrentHashMap<>();

    public ProductApiClient(WebClient externalProductsWebClient, ExternalProductsApiProperties properties) {
        this.webClient = externalProductsWebClient;
        this.timeout = Duration.ofMillis(properties.responseTimeoutMs());
        this.cacheTtl = properties.cacheTtl();
    }

    @Override
    public Mono<List<String>> findSimilarProductIds(String productId) {
        return similarIdsCache.computeIfAbsent(productId, this::fetchSimilarProductIds)
                .onErrorResume(error -> {
                    similarIdsCache.remove(productId);
                    return Mono.error(error);
                });
    }

    @Override
    public Mono<Product> findProductById(String productId) {
        return productCache.computeIfAbsent(productId, this::fetchProductOptional)
                .flatMap(optional -> optional.map(Mono::just).orElseGet(Mono::empty))
                .onErrorResume(error -> {
                    productCache.remove(productId);
                    return Mono.empty();
                });
    }

    private Mono<List<String>> fetchSimilarProductIds(String productId) {
        return webClient.get()
                .uri("/product/{productId}/similarids", productId)
                .exchangeToMono(response -> {
                    if (response.statusCode().value() == HTTP_NOT_FOUND) {
                        return Mono.error(new ProductNotFoundException(productId));
                    }
                    if (response.statusCode().isError()) {
                        return Mono.error(new ExternalProductApiException(
                                "External API failed while fetching similar ids for product " + productId
                        ));
                    }
                    return response.bodyToMono(STRING_LIST);
                })
                .timeout(timeout)
                .onErrorMap(error -> mapSimilarIdsError(productId, error))
                .cache(cacheTtl);
    }

    private Mono<Optional<Product>> fetchProductOptional(String productId) {
        return webClient.get()
                .uri("/product/{productId}", productId)
                .exchangeToMono(response -> {
                    if (response.statusCode().isError()) {
                        return Mono.just(Optional.<Product>empty());
                    }
                    return response.bodyToMono(Product.class).map(Optional::of);
                })
                .timeout(timeout)
                .doOnError(error -> log.debug("Ignoring unavailable similar product {}", productId, error))
                .onErrorReturn(Optional.empty())
                .cache(cacheTtl);
    }

    private Throwable mapSimilarIdsError(String productId, Throwable error) {
        Throwable unwrappedError = Exceptions.unwrap(error);
        if (isExpectedSimilarIdsError(unwrappedError)) {
            return unwrappedError;
        }
        return new ExternalProductApiException(
                "External API failed while fetching similar ids for product " + productId,
                error
        );
    }

    private boolean isExpectedSimilarIdsError(Throwable unwrappedError) {
        return unwrappedError instanceof ProductNotFoundException
                || unwrappedError instanceof ExternalProductApiException;
    }
}
