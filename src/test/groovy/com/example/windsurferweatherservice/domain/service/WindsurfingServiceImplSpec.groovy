package com.example.windsurferweatherservice.domain.service

import com.example.windsurferweatherservice.infra.WeatherBitClient
import com.example.windsurferweatherservice.application.controller.dto.DailyForecast
import com.example.windsurferweatherservice.application.controller.dto.LocationForecast
import com.example.windsurferweatherservice.application.controller.dto.WeatherForecast
import com.example.windsurferweatherservice.domain.model.Location
import com.example.windsurferweatherservice.domain.storage.LocationStorage
import com.example.windsurferweatherservice.domain.validator.WeatherValidator
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.LocalDate

class WindsurfingServiceImplSpec extends Specification {

    def repository = Mock(LocationStorage)
    def weatherBitClient = Mock(WeatherBitClient)
    def weatherValidator = Mock(WeatherValidator)

    @Subject
    def service = new WindsurfingServiceImpl(repository, weatherBitClient, weatherValidator)

    @Shared
    def jastarnia = new Location("Jastarnia", "Poland", 54.7, 18.67),
        bridgetown = new Location("Bridgetown", "Barbados", 13.1, -59.6),
        fortaleza = new Location("Fortaleza", "Brazil", -3.7, -38.5)

    @Shared
    def jastarniaDailyForecast = new DailyForecast("2025-10-01", 20.0, 10.0),
        bridgetownDailyForecast = new DailyForecast("2025-10-01", 25.0, 15.0)


    def "getAllLocations should return all locations from repository"() {
        given:
        def locations = [jastarnia, bridgetown, fortaleza]

        and:
        repository.findAll() >> locations

        when:
        def result = service.getAllLocations()

        then:
        result == locations
        result.size() == 3
    }

    @Unroll
    def "getForecastForDate should return correct forecast for date"() {
        given:
        def date = LocalDate.parse(dateStr)
        def forecasts = [
                new DailyForecast("2025-10-01", 20.0, 10.0),
                new DailyForecast("2025-10-02", 22.0, 12.0),
                new DailyForecast("2025-10-03", 18.0, 8.0)
        ]

        and:
        weatherBitClient.getForecast(_, _) >> new WeatherForecast(forecasts, locationName, countryCode)
        weatherValidator.isForecastValid(_) >> true


        when:
        def result = service.getForecastForDate(location, date)

        then:
        result.isPresent()
        result.get().validDate() == dateStr
        result.get().temp() == expectedTemp
        result.get().windSpd() == expectedWind

        where:
        location   | locationName | countryCode | dateStr      | expectedTemp | expectedWind
        jastarnia  | "Jastarnia"  | "Poland"    | "2025-10-01" | 20.0d        | 10.0d
        bridgetown | "Bridgetown" | "Barbados"  | "2025-10-02" | 22.0d        | 12.0d
        fortaleza  | "Fortaleza"  | "Brazil"    | "2025-10-03" | 18.0d        | 8.0d
    }

    def "getForecastForDate should return empty when forecast is invalid"() {
        given:
        def date = LocalDate.of(2025, 10, 01)
        def jastarniaWeatherForecast = new WeatherForecast([jastarniaDailyForecast], "Jastarnia", "Poland")


        and:
        weatherBitClient.getForecast(_, _) >> jastarniaWeatherForecast
        weatherValidator.isForecastValid(_) >> false

        when:
        def result = service.getForecastForDate(jastarnia, date)

        then:
        !result.isPresent()
    }

    def "getForecastForDate should return empty when exception occurs"() {
        given:
        def date = LocalDate.of(2025, 10, 1)

        and:
        weatherBitClient.getForecast(_, _) >> { throw new RuntimeException("API error") }

        when:
        def result = service.getForecastForDate(jastarnia, date)

        then:
        !result.isPresent()
    }

    def "findBestSuitableLocation should return location with highest score"() {
        given:
        def forecast1 = new DailyForecast("2025-10-01", 20.0, 10.0)
        def forecast2 = new DailyForecast("2025-10-01", 25.0, 15.0)
        def forecast3 = new DailyForecast("2025-10-01", 30.0, 8.0)
        def locationForecasts = [
                new LocationForecast(jastarnia, Optional.of(forecast1)),
                new LocationForecast(bridgetown, Optional.of(forecast2)),
                new LocationForecast(fortaleza, Optional.of(forecast3))
        ]
        def date = LocalDate.of(2025, 10, 1)

        and:
        weatherValidator.isSuitableForWindsurfing(Optional.of(forecast1)) >> true
        weatherValidator.isSuitableForWindsurfing(Optional.of(forecast2)) >> true
        weatherValidator.isSuitableForWindsurfing(Optional.of(forecast3)) >> true

        and:
        weatherValidator.calculateScore(Optional.of(forecast1)) >> 50.0
        weatherValidator.calculateScore(Optional.of(forecast2)) >> 70.0
        weatherValidator.calculateScore(Optional.of(forecast3)) >> 60.0

        when:
        def result = service.findBestSuitableLocation(locationForecasts, date)

        then:
        result.isPresent()
        result.get().location() == bridgetown.name()
        result.get().country() == bridgetown.country()
        result.get().date() == date
        result.get().windSpeed() == bridgetownDailyForecast.windSpd()
        result.get().temperature() == bridgetownDailyForecast.temp()
    }

    def "findBestSuitableLocation should return empty when no suitable locations"() {
        given:
        def forecast = new DailyForecast("2025-10-01", 20.0, 10.0)
        def locationForecasts = [new LocationForecast(jastarnia, Optional.of(forecast))]
        def date = LocalDate.of(2025, 10, 1)

        and:
        weatherValidator.isSuitableForWindsurfing(_) >> false

        when:
        def result = service.findBestSuitableLocation(locationForecasts, date)

        then:
        !result.isPresent()
    }


    @Unroll
    def "buildResponse should return correct WindsurfingResponse"() {
        given:
        def forecast = new DailyForecast(dateStr, temp, wind)
        def date = LocalDate.of(2025, 10, 01)

        when:
        def result = service.buildResponse(location, Optional.of(forecast), date)

        then:
        result.location() == location.name()
        result.country() == location.country()
        result.date() == date
        result.temperature() == forecast.temp()
        result.windSpeed() == forecast.windSpd()

        where:
        location   | locationName | country    | dateStr      | temp | wind
        jastarnia  | "Jastarnia"  | "Poland"   | "2025-10-01" | 20.0 | 10.0
        bridgetown | "Bridgetown" | "Barbados" | "2025-10-01" | 25.0 | 15.0
        fortaleza  | "Fortaleza"  | "Brazil"   | "2025-10-01" | 30.0 | 12.0

    }

}
