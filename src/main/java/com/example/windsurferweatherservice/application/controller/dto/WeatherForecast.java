package com.example.windsurferweatherservice.application.controller.dto;

import java.util.List;

public record WeatherForecast(List<DailyForecast> data, String cityName, String country) {
}
