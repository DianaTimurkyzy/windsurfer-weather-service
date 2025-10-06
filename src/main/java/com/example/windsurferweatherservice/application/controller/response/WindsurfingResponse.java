package com.example.windsurferweatherservice.application.controller.response;

import java.time.LocalDate;

public record WindsurfingResponse(String location, String country, LocalDate date, double temperature, double windSpeed) {
}
