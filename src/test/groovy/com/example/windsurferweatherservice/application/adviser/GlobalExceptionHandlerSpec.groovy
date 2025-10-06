package com.example.windsurferweatherservice.application.adviser


import com.example.windsurferweatherservice.application.adviser.exception.NoAvailableLocationsException
import com.example.windsurferweatherservice.application.adviser.exception.NoSuitableLocationException
import com.example.windsurferweatherservice.application.adviser.exception.WeatherApiException
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.LocalDate

class GlobalExceptionHandlerSpec extends Specification {

    @Subject
    def handler = new GlobalExceptionHandler()

    def "should handle NoSuitableLocationException and return 404"() {
        given:
        def exception = new NoSuitableLocationException("No suitable location found for the given date")

        when:
        def response = handler.handleNoSuitableLocation(exception)

        then:
        response.statusCode == HttpStatus.NOT_FOUND
        response.body.message() == "No suitable location found for the given date"
        response.statusCode.value() == 404
    }

    def "should handle NoLocationsAvailableException and return 500"() {
        given:
        def exception = new NoAvailableLocationsException("No locations configured in the system")

        when:
        def response = handler.handleNoLocationsAvailable(exception)

        then:
        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
        response.body.message() == "No locations configured in the system"
        response.statusCode.value() == 500
    }

    def "should handle WeatherApiException and return 503"() {
        given:
        def cause = new RuntimeException("Connection timeout")
        def exception = new WeatherApiException("Failed to fetch weather data", cause)

        when:
        def response = handler.handleWeatherApiException(exception)

        then:
        with(response) { // TODO
            statusCode == HttpStatus.SERVICE_UNAVAILABLE
            body.message() == "Failed to fetch weather data"
            statusCode.value() == 503
        }
    }

    def "should handle MethodArgumentNotValidException and return 400 with errors map"() {
        given:
        def fieldError = new FieldError("object", "date", "must be in the future")
        def bindingResult = Mock(BindingResult)
        bindingResult.getAllErrors() >> [fieldError]

        def exception = new MethodArgumentNotValidException(null, bindingResult)

        when:
        def response = handler.handleValidationExceptions(exception)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.body.size() == 1
        response.body["date"] == "must be in the future"
    }

    def "should handle multiple validation errors"() {
        given:
        def fieldError1 = new FieldError("object", "date", "must not be null")
        def fieldError2 = new FieldError("object", "location", "must not be blank")
        def bindingResult = Mock(BindingResult)
        bindingResult.getAllErrors() >> [fieldError1, fieldError2]

        def exception = new MethodArgumentNotValidException(null, bindingResult)

        when:
        def response = handler.handleValidationExceptions(exception)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.body.size() == 2
        response.body["date"] == "must not be null"
        response.body["location"] == "must not be blank"
    }

    def "should handle MethodArgumentTypeMismatchException and return 400"() {
        given:
        def exception = new MethodArgumentTypeMismatchException( // TODO
                "invalid-date",
                LocalDate.class,
                "date",
                null,
                null
        )

        when:
        def response = handler.handleTypeMismatch(exception)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.body.message() == "Invalid parameter format: date"
        response.statusCode.value() == 400
    }

    def "should handle generic Exception and return 500"() {
        given:
        def exception = new RuntimeException("Unexpected error occurred")

        when:
        def response = handler.handleGenericException(exception)

        then:
        response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR
        response.body.message() == "Unexpected error occurred"
        response.statusCode.value() == 500
    }

    @Unroll
    def "should handle #exceptionType with appropriate status code"() {
        when:
        def response = handler."$handlerMethod"(exception)

        then:
        response.statusCode == expectedStatus

        where:
        exceptionType                   | handlerMethod                | exception                                                         || expectedStatus
        "NoSuitableLocationException"   | "handleNoSuitableLocation"   | new NoSuitableLocationException("No location")                    || HttpStatus.NOT_FOUND
        "NoAvailableLocationsException" | "handleNoLocationsAvailable" | new NoAvailableLocationsException("No locations") || HttpStatus.INTERNAL_SERVER_ERROR
        "WeatherApiException"           | "handleWeatherApiException"  | new WeatherApiException("API failed", new IOException("timeout")) || HttpStatus.SERVICE_UNAVAILABLE
        "Generic Exception"             | "handleGenericException"     | new RuntimeException("Error")                                     || HttpStatus.INTERNAL_SERVER_ERROR
    }

    def "should return empty map when no validation errors"() {
        given:
        def bindingResult = Mock(BindingResult)
        bindingResult.getAllErrors() >> []

        def exception = new MethodArgumentNotValidException(null, bindingResult)

        when:
        def response = handler.handleValidationExceptions(exception)

        then:
        response.statusCode == HttpStatus.BAD_REQUEST
        response.body.isEmpty()
    }
}