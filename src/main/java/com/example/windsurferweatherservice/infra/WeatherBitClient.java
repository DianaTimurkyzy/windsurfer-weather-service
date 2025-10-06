package com.example.windsurferweatherservice.infra;

import com.example.windsurferweatherservice.application.controller.dto.WeatherForecast;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class WeatherBitClient {

    private final WebClient webClient;

    @Value("${weatherbit.api.key}")
    private String apiKey;

    public WeatherForecast getForecast(double latitude, double longitude) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast/daily")
                        .queryParam("lat", latitude)
                        .queryParam("lon", longitude)
                        .queryParam("key", apiKey)
                        .queryParam("days", 7)
                .build())
                .retrieve()
                .bodyToMono(WeatherForecast.class)
                .timeout(Duration.ofSeconds(10))
                .block();
    }

}
