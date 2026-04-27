package com.example.similarproducts.domain.port;

import com.example.similarproducts.domain.model.Product;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Outbound port that exposes the product catalog capabilities required by the application layer.
 */
public interface ProductCatalogPort {

    /**
     * Finds ordered ids of products similar to the given source product.
     */
    Mono<List<String>> findSimilarProductIds(String productId);

    /**
     * Finds one product detail by id.
     */
    Mono<Product> findProductById(String productId);
}
