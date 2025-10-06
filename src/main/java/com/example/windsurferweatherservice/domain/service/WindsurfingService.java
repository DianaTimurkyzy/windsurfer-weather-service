package com.example.windsurferweatherservice.domain.service;

import com.example.windsurferweatherservice.application.controller.dto.DailyForecast;
import com.example.windsurferweatherservice.application.controller.dto.LocationForecast;
import com.example.windsurferweatherservice.application.controller.response.WindsurfingResponse;
import com.example.windsurferweatherservice.domain.model.Location;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WindsurfingService {

    List<Location> getAllLocations();

    Optional<DailyForecast> getForecastForDate(Location location, LocalDate date);

    Optional<WindsurfingResponse> findBestSuitableLocation(List<LocationForecast> forecasts, LocalDate date);
}
