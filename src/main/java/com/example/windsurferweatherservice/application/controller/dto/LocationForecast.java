package com.example.windsurferweatherservice.application.controller.dto;

import com.example.windsurferweatherservice.domain.model.Location;
import java.util.Optional;

public record LocationForecast(Location location, Optional<DailyForecast> forecast) {
}