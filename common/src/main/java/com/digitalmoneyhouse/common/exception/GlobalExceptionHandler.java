package com.digitalmoneyhouse.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
        ResourceNotFoundException exception,
        HttpServletRequest request
    ) {
        return error(
            HttpStatus.NOT_FOUND,
            exception.getMessage(),
            request
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(
        ConflictException exception,
        HttpServletRequest request
    ) {
        return error(
            HttpStatus.CONFLICT,
            exception.getMessage(),
            request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        return error(
            HttpStatus.BAD_REQUEST,
            "Los datos enviados no son válidos",
            request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
        Exception exception,
        HttpServletRequest request
    ) {
        log.error(
            "Error inesperado al procesar {}",
            request.getRequestURI(),
            exception
        );

        return error(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Ocurrió un error inesperado",
            request
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
        UnauthorizedException exception,
        HttpServletRequest request
    ) {
        return error(
            HttpStatus.UNAUTHORIZED,
            exception.getMessage(),
            request
        );
    }

    private ResponseEntity<ApiError> error(
        HttpStatus status,
        String message,
        HttpServletRequest request
    ) {
        ApiError body = new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI()
        );

        return ResponseEntity.status(status).body(body);
    }
}