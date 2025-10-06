package com.example.windsurferweatherservice.domain;

import com.example.windsurferweatherservice.application.controller.dto.LocationForecast;
import com.example.windsurferweatherservice.application.controller.response.WindsurfingResponse;
import com.example.windsurferweatherservice.domain.service.WindsurfingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WindsurfingFacade {

    private final WindsurfingService service;

    public Optional<WindsurfingResponse> getBestLocation(LocalDate date) {
        var locations = service.getAllLocations();
        List<LocationForecast> forecasts = locations.stream()
                .map(location -> new LocationForecast(
                        location,
                        service.getForecastForDate(location, date)
                ))
                .filter(lf -> lf.forecast().isPresent())
                .toList();

        return service.findBestSuitableLocation(forecasts, date);
    }

}
