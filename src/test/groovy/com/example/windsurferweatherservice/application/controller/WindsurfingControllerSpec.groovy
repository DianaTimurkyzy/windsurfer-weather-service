package com.example.windsurferweatherservice.application.controller

import com.example.windsurferweatherservice.application.adviser.exception.NoSuitableLocationException
import com.example.windsurferweatherservice.application.controller.response.WindsurfingResponse
import com.example.windsurferweatherservice.domain.WindsurfingFacade
import spock.lang.Specification
import spock.lang.Subject

import java.time.LocalDate

class WindsurfingControllerSpec extends Specification {

    def facade = Mock(WindsurfingFacade)

    @Subject
    def windsurfingController = new WindsurfingController(facade)

    def "should get the best windsurfing location"() {
        given:
        def date = LocalDate.of(2025, 10, 15)
        def response = new WindsurfingResponse("Bridgetown", "Barbados", date, 25.0, 15.0)

        and:
        facade.getBestLocation(date) >> Optional.of(response)
        when:
        def result = windsurfingController.getBestLocation(date)

        then:
        result == response
        result.location() == "Bridgetown"
        result.country() == "Barbados"
        result.temperature() == 25.0d
        result.windSpeed() == 15.0d
    }

    def "should handle when no suitable location is found"() {
        given:
        def date = LocalDate.of(2025, 10, 15)

        and:
        facade.getBestLocation(date) >> Optional.empty()

        when:
        windsurfingController.getBestLocation(date)

        then:
        def exception = thrown(NoSuitableLocationException)
        exception.message.contains("No suitable location found for date: 2025-10-15")
    }

}
