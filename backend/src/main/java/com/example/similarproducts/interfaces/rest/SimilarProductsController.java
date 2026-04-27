package com.example.similarproducts.interfaces.rest;

import com.example.similarproducts.application.usecase.GetSimilarProductsUseCase;
import com.example.similarproducts.domain.model.Product;
import com.example.similarproducts.interfaces.rest.dto.ProductResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller that exposes similar products and maps domain products to response DTOs.
 */
@RestController
@RequestMapping("/product")
public class SimilarProductsController {

    private final GetSimilarProductsUseCase getSimilarProductsUseCase;

    public SimilarProductsController(GetSimilarProductsUseCase getSimilarProductsUseCase) {
        this.getSimilarProductsUseCase = getSimilarProductsUseCase;
    }

    /**
     * Handles the similar-products endpoint for one source product.
     */
    @GetMapping("/{productId}/similar")
    public Mono<ResponseEntity<List<ProductResponse>>> getSimilarProducts(@PathVariable String productId) {
        return getSimilarProductsUseCase.execute(productId)
                .map(products -> products.stream()
                        .map(this::toResponse)
                        .toList()
                )
                .map(ResponseEntity::ok);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.id(),
                product.name(),
                product.price(),
                product.availability()
        );
    }
}
