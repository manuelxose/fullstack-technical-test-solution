package com.example.similarproducts.interfaces.rest;

import com.example.similarproducts.infrastructure.http.client.ExternalProductApiException;
import com.example.similarproducts.infrastructure.http.client.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps application exceptions to the REST status codes defined by the API contract.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Void> handleProductNotFound() {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(ExternalProductApiException.class)
    public ResponseEntity<Void> handleExternalApiFailure() {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }
}
