package com.ajcarpinello.Pern_Merch_Website.controller;

import com.ajcarpinello.Pern_Merch_Website.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

// this class acts like a globalized catch block for the whole app

// @RestControllerAdvice intercepts exceptions thrown by any controller in the app
// and converts them into clean JSON responses instead of ugly HTML error pages.
// This gives the frontend a predictable error format it can display to the user.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Catches our custom AppException and returns the HTTP status code we chose when throwing it.
    // e.g. 404 for "not found", 409 for "conflict", 400 for bad input.
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, String>> handleAppException(AppException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
    }

    // Fallback: catches any unexpected RuntimeException and returns a generic 500 error.
    // The real error message is NOT exposed to the client for security reasons.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred"));
    }

    // BadCredentialsException is NOT a RuntimeException — it extends AuthenticationException,
    // so it won't be caught by the handlers above. It needs its own handler.
    // Spring Security automatically throws this when authenticationManager.authenticate()
    // fails (i.e., wrong username or password), even though we never throw it ourselves.
    // Returns 401 Unauthorized with a generic message (intentionally vague for security).
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid username or password"));
    }
}
