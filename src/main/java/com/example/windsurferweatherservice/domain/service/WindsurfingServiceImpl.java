package com.example.windsurferweatherservice.domain.service;

import com.example.windsurferweatherservice.application.controller.dto.DailyForecast;
import com.example.windsurferweatherservice.application.controller.dto.LocationForecast;
import com.example.windsurferweatherservice.application.controller.dto.WeatherForecast;
import com.example.windsurferweatherservice.application.controller.response.WindsurfingResponse;
import com.example.windsurferweatherservice.domain.model.Location;
import com.example.windsurferweatherservice.domain.storage.LocationStorage;
import com.example.windsurferweatherservice.domain.validator.WeatherValidator;
import com.example.windsurferweatherservice.infra.WeatherBitClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WindsurfingServiceImpl implements WindsurfingService {

    private final LocationStorage storage;
    private final WeatherBitClient weatherBitClient;
    private final WeatherValidator weatherValidator;


    @Override
    public List<Location> getAllLocations() {
        return storage.findAll();
    }

    @Override
    public Optional<DailyForecast> getForecastForDate(Location location, LocalDate date) {
        try {
            WeatherForecast forecast = weatherBitClient.getForecast(
                    location.latitude(),
                    location.longitude()
            );

            if (!weatherValidator.isForecastValid(forecast)) {
                return Optional.empty();
            }

            return forecast.data().stream()
                    .filter(f -> f.validDate().equals(date.toString()))
                    .findFirst();
        } catch (Exception e) {
            log.error("Error fetching forecast for {}: {}", location.name(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<WindsurfingResponse> findBestSuitableLocation(List<LocationForecast> forecasts, LocalDate date) {
        return forecasts.stream()
                .filter(lf -> weatherValidator.isSuitableForWindsurfing(lf.forecast()))
                .max(Comparator.comparingDouble(lf -> weatherValidator.calculateScore(lf.forecast())))
                .map(lf -> buildResponse(lf.location(), lf.forecast(), date));
    }

    WindsurfingResponse buildResponse(Location location, Optional<DailyForecast> forecast, LocalDate date) {
        return new WindsurfingResponse(
                location.name(),
                location.country(),
                date,
                forecast.get().temp(),
                forecast.get().windSpd()
        );
    }
}