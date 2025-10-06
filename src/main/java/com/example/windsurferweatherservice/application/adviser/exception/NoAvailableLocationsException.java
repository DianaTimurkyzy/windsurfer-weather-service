package com.example.windsurferweatherservice.application.adviser.exception;

public class NoAvailableLocationsException extends RuntimeException {

    public NoAvailableLocationsException(String message) {
        super(message);
    }
}