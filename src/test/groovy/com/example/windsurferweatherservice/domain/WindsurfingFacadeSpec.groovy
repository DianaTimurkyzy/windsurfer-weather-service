package com.example.windsurferweatherservice.domain

import com.example.windsurferweatherservice.application.controller.dto.DailyForecast
import com.example.windsurferweatherservice.application.controller.response.WindsurfingResponse
import com.example.windsurferweatherservice.domain.model.Location
import com.example.windsurferweatherservice.domain.service.WindsurfingService
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate


class WindsurfingFacadeSpec extends Specification {

    def windsurfingService = Mock(WindsurfingService)

    @Subject
    def facade = new WindsurfingFacade(windsurfingService)

    @Shared
    def jastarnia = new Location("Jastarnia", "Poland", 54.7, 18.67),
        bridgetown = new Location("Bridgetown", "Barbados", 13.1, -59.6),
        fortaleza = new Location("Fortaleza", "Brazil", -3.7, -38.5)

    @Shared
    def jastarniaDailyForecast = new DailyForecast("2025-10-01", 20.0, 10.0),
        bridgetownDailyForecast = new DailyForecast("2025-10-01", 25.0, 15.0),
        fortalezaDailyForecast = new DailyForecast("2025-10-01", 30.0, 12.0)

    def "should return best windsurfing location"() {
        given:
        def locations = [jastarnia, bridgetown, fortaleza]
        def date = LocalDate.of(2025, 10, 01)
        def expectedBestLocation = new WindsurfingResponse("Bridgetown", "Barbados", date, 25.0, 15.0)

        and:
        windsurfingService.getAllLocations() >> locations

        and:
        windsurfingService.getForecastForDate(jastarnia, date) >> Optional.of(jastarniaDailyForecast)
        windsurfingService.getForecastForDate(bridgetown, date) >> Optional.of(bridgetownDailyForecast)
        windsurfingService.getForecastForDate(fortaleza, date) >> Optional.of(fortalezaDailyForecast)

        and:
        windsurfingService.findBestSuitableLocation(_, date) >> Optional.of(expectedBestLocation)

        when:
        def result = facade.getBestLocation(date)

        then:
        result.isPresent()
        result.get().location() == expectedBestLocation.location()
        result.get().country() == expectedBestLocation.country()
        result.get().date() == date
        result.get().temperature() == expectedBestLocation.temperature()
        result.get().windSpeed() == expectedBestLocation.windSpeed()
    }

    def "should return empty when no suitable location found"() {
        given:
        def locations = [jastarnia, bridgetown, fortaleza]
        def date = LocalDate.of(2025, 10, 1)

        and:
        windsurfingService.getAllLocations() >> locations

        and:
        windsurfingService.getForecastForDate(jastarnia, date) >> Optional.of(jastarniaDailyForecast)
        windsurfingService.getForecastForDate(bridgetown, date) >> Optional.of(bridgetownDailyForecast)
        windsurfingService.getForecastForDate(fortaleza, date) >> Optional.of(fortalezaDailyForecast)

        and:
        windsurfingService.findBestSuitableLocation(_, date) >> Optional.empty()

        when:
        def result = facade.getBestLocation(date)

        then:
        !result.isPresent()
    }

    def "should filter out locations without forecasts"() {
        given:
        def locations = [jastarnia, bridgetown, fortaleza]
        def date = LocalDate.of(2025, 10, 1)
        def expectedBestLocation = new WindsurfingResponse("Jastarnia", "Poland", date, 20.0, 10.0)

        and:
        windsurfingService.getAllLocations() >> locations
        windsurfingService.getForecastForDate(jastarnia, date) >> Optional.of(jastarniaDailyForecast)
        windsurfingService.getForecastForDate(bridgetown, date) >> Optional.empty()
        windsurfingService.getForecastForDate(fortaleza, date) >> Optional.empty()
        windsurfingService.findBestSuitableLocation(_, date) >> Optional.of(expectedBestLocation)

        when:
        def result = facade.getBestLocation(date)

        then:
        result.isPresent()
        result.get().location() == jastarnia.name()
    }
}