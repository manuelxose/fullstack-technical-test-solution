package com.example.similarproducts.infrastructure.http.client;

/**
 * Signals that the requested source product does not exist in the external catalog.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String productId) {
        super("Product not found: " + productId);
    }
}
