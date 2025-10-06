package com.example.windsurferweatherservice.domain.validator;

import com.example.windsurferweatherservice.application.controller.dto.DailyForecast;
import com.example.windsurferweatherservice.application.controller.dto.WeatherForecast;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WeatherValidator {

    private static final double MIN_WIND_SPEED = 5.0;
    private static final double MAX_WIND_SPEED = 18.0;
    private static final double MIN_TEMPERATURE = 5.0;
    private static final double MAX_TEMPERATURE = 35.0;
    private static final int WIND_SCORE_MULTIPLIER = 3;

    public boolean isForecastValid(WeatherForecast forecast) {
        return forecast != null
               && forecast.data() != null
               && !forecast.data().isEmpty();
    }

    public boolean isSuitableForWindsurfing(Optional<DailyForecast> forecast) {
        if (forecast.isEmpty()) return false;

        double windSpeed = forecast.get().windSpd();
        double temperature = forecast.get().temp();

        boolean windOk = isWindSpeedSuitable(windSpeed);
        boolean tempOk = isTemperatureSuitable(temperature);

        return windOk && tempOk;
    }

    boolean isWindSpeedSuitable(double windSpeed) {
        return windSpeed >= MIN_WIND_SPEED && windSpeed <= MAX_WIND_SPEED;
    }

    boolean isTemperatureSuitable(double temperature) {
        return temperature >= MIN_TEMPERATURE && temperature <= MAX_TEMPERATURE;
    }

    public double calculateScore(Optional<DailyForecast> forecast) {
        return forecast.get().windSpd() * WIND_SCORE_MULTIPLIER + forecast.get().temp();
    }

}
