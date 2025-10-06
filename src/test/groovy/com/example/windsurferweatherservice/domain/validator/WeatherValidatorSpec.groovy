package com.example.windsurferweatherservice.domain.validator

import com.example.windsurferweatherservice.application.controller.dto.DailyForecast
import com.example.windsurferweatherservice.application.controller.dto.WeatherForecast
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class WeatherValidatorSpec extends Specification {

    @Subject
    def validator = new WeatherValidator()

    def "isForecastValid should return true when forecast is valid"() {
        given:
        def forecast = new WeatherForecast(
                [new DailyForecast("2025-10-01", 20.0, 10.0)],
                "Jastarnia",
                "Poland"
        )

        when:
        def result = validator.isForecastValid(forecast)

        then:
        result == true
    }

    @Unroll
    def "isForecastValid should return false when forecast is #scenario"() {
        when:
        def result = validator.isForecastValid(forecast)

        then:
        result == false

        where:
        scenario        | forecast
        "null"          | null
        "null data"     | new WeatherForecast(null, "Location", "Country")
        "empty data"    | new WeatherForecast(Collections.emptyList(), "Location", "Country")
    }

    @Unroll
    def "isSuitableForWindsurfing should return #expected for wind=#wind and temp=#temp "() {
        given:
        def dailyForecast = new DailyForecast("2025.10.01", temp, wind)

        when:
        def result = validator.isSuitableForWindsurfing(Optional.of(dailyForecast))

        then:
        result == expected

        where:
        wind | temp | expected
        10.0 | 20.0 | true
        5.0  | 5.0  | true
        18.0 | 35.0 | true
        4.9  | 20.0 | false
        4.0  | 20.0 | false
        10.0 | 3.0  | false
        10.0 | 35.1 | false
        10.0 | 40.0 | false
    }

    def "isSuitableForWindsurfing should return false when forecast is empty"() {
        when:
        def result = validator.isSuitableForWindsurfing(Optional.empty())

        then:
        result == false
    }

    @Unroll
    def "should validate wind speed: #windSpeed -> #expected"() {
        when:
        def result = validator.isWindSpeedSuitable(windSpeed)

        then:
        result == expected

        where:
        windSpeed | expected
        10.0      | true
        4.9       | false
        5.0       | true
        18.0      | true
        18.1      | false

    }

    @Unroll
    def "should validate temperature: #temperature -> #expected"() {
        when:
        def result = validator.isTemperatureSuitable(temperature)

        then:
        result == expected

        where:
        temperature | expected
        4.0         | false
        5.0         | true
        15.0        | true
        35.0        | true
        36.1        | false
    }

    @Unroll
    def "calculateScore should return correct score for wind=#wind and temp=#temp"() {
        given:
        def forecast = Optional.of(new DailyForecast("2025-10-01", temp, wind))

        when:
        def result = validator.calculateScore(forecast)

        then:
        result == expectedScore

        where:
        wind | temp | expectedScore
        10.0 | 20.0 | 50.0d
        15.0 | 25.0 | 70.0d
    }
}
