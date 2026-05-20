package br.com.arenamatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder,
                                 @Value("${arenamatch.api-base-url:http://localhost:8080}") String apiBaseUrl) {
        // Configura a URL base para todas as chamadas internas
        return builder
                .baseUrl(apiBaseUrl)
                .build();
    }
}
