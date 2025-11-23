package org.example.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Configuration
public class WebClientConfig {
    @Value("${account-service.base-url}")
    private String accountServiceUrl;

    @Bean
    public WebClient accountServiceWebClient() {
        return WebClient.builder()
                .baseUrl(accountServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(basicAuthenticationFilter())
                .build();
    }

    private ExchangeFilterFunction basicAuthenticationFilter() {
        return (clientRequest, next) -> {
            String credentials = "admin:admin";
            String authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString(credentials.getBytes());

            ClientRequest authenticatedRequest = ClientRequest.from(clientRequest)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .build();

            return next.exchange(authenticatedRequest);
        };
    }
}
