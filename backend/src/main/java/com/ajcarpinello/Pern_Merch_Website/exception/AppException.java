package com.ajcarpinello.Pern_Merch_Website.exception;

import org.springframework.http.HttpStatus;

// A single custom exception that carries an HTTP status code.
// Throw this anywhere in the app instead of RuntimeException.
// The GlobalExceptionHandler will catch it and return the correct HTTP response.
public class AppException extends RuntimeException {

    private final HttpStatus status;

    public AppException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
