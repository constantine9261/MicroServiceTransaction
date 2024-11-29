package com.bank.microserviceTransaction.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {


    @Bean
    public WebClient accountWebClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8081/accounts") // URL base del servicio de cuentas
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
