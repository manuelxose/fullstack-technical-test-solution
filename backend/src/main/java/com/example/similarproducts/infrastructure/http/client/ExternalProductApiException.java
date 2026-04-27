package com.example.similarproducts.infrastructure.http.client;

/**
 * Signals an unexpected downstream failure when calling the external product API.
 */
public class ExternalProductApiException extends RuntimeException {

    public ExternalProductApiException(String message) {
        super(message);
    }

    public ExternalProductApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
