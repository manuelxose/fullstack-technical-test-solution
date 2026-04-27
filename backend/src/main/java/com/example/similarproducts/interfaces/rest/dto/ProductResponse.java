package com.example.similarproducts.interfaces.rest.dto;

import java.math.BigDecimal;

/**
 * Response DTO returned by the similar-products REST endpoint.
 */
public record ProductResponse(
        String id,
        String name,
        BigDecimal price,
        boolean availability
) {}
