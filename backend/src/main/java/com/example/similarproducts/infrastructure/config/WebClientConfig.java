package com.example.similarproducts.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Creates the HTTP client used to call the external product API.
 */
@Configuration
public class WebClientConfig {

    /**
     * Builds a WebClient with bounded connection pooling and request timeouts.
     */
    @Bean
    public WebClient externalProductsWebClient(ExternalProductsApiProperties properties) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("external-products-api")
                .maxConnections(properties.maxConnections())
                .pendingAcquireMaxCount(properties.pendingAcquireMaxCount())
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.connectTimeoutMs())
                .responseTimeout(Duration.ofMillis(properties.responseTimeoutMs()))
                .doOnConnected(connection -> connection.addHandlerLast(
                        new ReadTimeoutHandler(properties.responseTimeoutMs(), TimeUnit.MILLISECONDS)
                ));

        return WebClient.builder()
                .baseUrl(properties.baseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
