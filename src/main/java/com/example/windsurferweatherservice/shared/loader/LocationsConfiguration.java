package com.example.windsurferweatherservice.shared.loader;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class LocationsConfiguration {

    @NotEmpty(message = "At least one location must be configured")
    private List<LocationConfiguration> locations;

    @Data
    public static class LocationConfiguration {
        @NotBlank
        private String name;

        @NotBlank
        private String country;

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        private double latitude;

        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        private double longitude;
    }
}
