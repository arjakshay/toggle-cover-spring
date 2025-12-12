package com.togglecover.user.exception;

import com.togglecover.user.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(UserProfileNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUserProfileNotFound(UserProfileNotFoundException ex, WebRequest request) {
        log.error("User profile not found: {}", ex.getMessage());

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errorCode("USER_NOT_FOUND")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.NOT_FOUND.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                errors.put(violation.getPropertyPath().toString(), violation.getMessage()));

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Constraint violation")
                .errorCode("CONSTRAINT_VIOLATION")
                .data(errors)
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(AccessDeniedException ex) {
        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Access denied: " + ex.getMessage())
                .errorCode("ACCESS_DENIED")
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleGenericException(Exception ex, WebRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("An unexpected error occurred")
                .errorCode("INTERNAL_ERROR")
                .timestamp(LocalDateTime.now())
                .path(request.getDescription(false))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}