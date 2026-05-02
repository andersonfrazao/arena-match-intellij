package br.com.arenamatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        // Configura a URL base para todas as chamadas internas
        return builder
                .baseUrl("http://localhost:8080")
                .build();
    }
}