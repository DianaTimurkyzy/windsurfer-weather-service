package com.example.windsurferweatherservice.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${weatherbit.api.url}")
    private String weatherApiBaseUrl;

    @Bean
    public WebClient webClient(ObjectMapper weatherbitObjectMapper) {
        return WebClient.builder()
                .baseUrl(weatherApiBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> {
                    configurer.defaultCodecs()
                            .jackson2JsonDecoder(new Jackson2JsonDecoder(weatherbitObjectMapper));
                    configurer.defaultCodecs()
                            .jackson2JsonEncoder(new Jackson2JsonEncoder(weatherbitObjectMapper));
                    configurer
                            .defaultCodecs()
                            .maxInMemorySize(10 * 1024 * 1024);
                })
                .build();
    }
}
