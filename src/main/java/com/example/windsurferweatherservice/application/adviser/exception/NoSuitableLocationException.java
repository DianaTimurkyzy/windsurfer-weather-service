package com.example.windsurferweatherservice.application.adviser.exception;

public class NoSuitableLocationException extends RuntimeException {

    public NoSuitableLocationException(String message) {
        super(message);
    }
}