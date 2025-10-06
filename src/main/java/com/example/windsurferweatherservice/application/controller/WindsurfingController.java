package com.example.windsurferweatherservice.application.controller;

import com.example.windsurferweatherservice.application.adviser.exception.NoSuitableLocationException;
import com.example.windsurferweatherservice.application.controller.response.WindsurfingResponse;
import com.example.windsurferweatherservice.domain.WindsurfingFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/windsurfing")
@Tag(name = "Windsurfing", description = "API for finding best windsurfing locations")
public class WindsurfingController {

    private final WindsurfingFacade facade;

    @GetMapping("/best-location")
    @Operation(
            summary = "Get best windsurfing location",
            description = "Returns the best location for windsurfing based on weather conditions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Best location found"),
            @ApiResponse(responseCode = "404", description = "No suitable location found"),
            @ApiResponse(responseCode = "400", description = "Invalid date parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public WindsurfingResponse getBestLocation(
            @RequestParam
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @FutureOrPresent(message = "Date must be today or in the future")
            @NotNull(message = "Date is required")
            LocalDate date) {
        return facade.getBestLocation(date)
                .orElseThrow(() -> new NoSuitableLocationException("No suitable location found for date: " + date));
    }

    /*
     * NOTE: @FutureOrPresent validates the date relative to the server’s time zone.
     *
     * Potential improvement:
     * For more accurate validation, time zones of the locations could be taken into account.
     * For example, when it’s still 23:00 (September 30) in Poland (UTC+2),
     * it’s already 01:00 (October 1) in Mauritius (UTC+4).
     *
     * Solution: to use the latest time zone from the list of locations
     * as the reference point for validation or the maximum offset (UTC+14).:
     *
     * ZoneId latestTimezone = ZoneId.of("Indian/Mauritius"); // UTC+4
     * LocalDate latestToday = LocalDate.now(latestTimezone);
     * if (date.isBefore(latestToday)) {
     *     throw new ValidationException("Date is in the past for all locations");
     * }
     *
     * Current implementation uses simple validation because:
     * 1. Weatherbit API itself filters out unavailable dates
     * 2. If the date is unavailable for all locations → exception is returned
     * 3. This is an edge case, and it doesn't significantly impact the overall functionality
     */

}
