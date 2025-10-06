package com.example.windsurferweatherservice.shared.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class LocationsDataLoader {

    private final ObjectMapper mapper;

    public LocationsConfiguration load() {
        try {
            var resource = new ClassPathResource("data/locations.json");
            return mapper.readValue(resource.getInputStream(), LocationsConfiguration.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load locations configuration from data/locations.json", e);
        }
    }
}