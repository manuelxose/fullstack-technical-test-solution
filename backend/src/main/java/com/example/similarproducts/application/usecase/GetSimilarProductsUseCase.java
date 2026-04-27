package com.example.similarproducts.application.usecase;

import com.example.similarproducts.domain.model.Product;
import com.example.similarproducts.domain.port.ProductCatalogPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Application use case that resolves the ordered product details for products similar to a source product.
 */
@Service
public class GetSimilarProductsUseCase {

    private final ProductCatalogPort productCatalogPort;
    private final int detailConcurrency;

    public GetSimilarProductsUseCase(
            ProductCatalogPort productCatalogPort,
            @Value("${external-products-api.detail-concurrency:8}") int detailConcurrency
    ) {
        this.productCatalogPort = productCatalogPort;
        this.detailConcurrency = detailConcurrency;
    }

    /**
     * Returns similar product details preserving the similarity order from the catalog.
     */
    public Mono<List<Product>> execute(String productId) {
        return productCatalogPort.findSimilarProductIds(productId)
                .flatMapMany(ids -> {
                    List<String> uniqueIds = ids.stream().distinct().toList();
                    Map<String, Integer> orderById = preserveSimilarityOrder(uniqueIds);

                    return Flux.fromIterable(uniqueIds)
                            .flatMap(productCatalogPort::findProductById, detailConcurrency)
                            .sort(Comparator.comparingInt(product ->
                                    orderById.getOrDefault(product.id(), Integer.MAX_VALUE)
                            ));
                })
                .collectList();
    }

    private Map<String, Integer> preserveSimilarityOrder(List<String> ids) {
        Map<String, Integer> order = new LinkedHashMap<>();
        IntStream.range(0, ids.size())
                .forEach(index -> order.putIfAbsent(ids.get(index), index));
        return order;
    }
}
