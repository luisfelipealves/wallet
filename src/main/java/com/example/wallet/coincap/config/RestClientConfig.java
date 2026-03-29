package com.example.wallet.coincap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${coincap.api-token}")
    private String apiToken;

    @Value("${coincap.base-url}")
    private String baseUrl;

    @Bean
    public RestClient coinCapClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiToken)
                .build();
    }
}
