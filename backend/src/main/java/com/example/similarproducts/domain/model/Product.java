package com.example.similarproducts.domain.model;

import java.math.BigDecimal;

/**
 * Domain representation of a product returned by the product catalog.
 */
public record Product(
        String id,
        String name,
        BigDecimal price,
        boolean availability
) {
}
