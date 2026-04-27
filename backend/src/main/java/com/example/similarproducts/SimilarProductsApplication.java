package com.example.similarproducts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Spring Boot entry point for the Similar Products API.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SimilarProductsApplication {

    /**
     * Starts the application context.
     */
    public static void main(String[] args) {
        SpringApplication.run(SimilarProductsApplication.class, args);
    }
}
