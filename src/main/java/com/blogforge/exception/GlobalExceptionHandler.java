package com.blogforge.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityExistsException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest req) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        Map<String, String> validationErrors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((error) -> {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        });

        ExceptionResponse er = new ExceptionResponse(
                Instant.now(),
                httpStatus.value(),
                validationErrors.toString(),
                req.getServletPath()
        );
        return new ResponseEntity<>(er, httpStatus);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleEntityNotFoundException(EntityNotFoundException e, HttpServletRequest req) {
        HttpStatus httpStatus = HttpStatus.NOT_FOUND;
        ExceptionResponse er = new ExceptionResponse(
                Instant.now(),
                httpStatus.value(),
                e.getMessage(),
                req.getServletPath()
        );
        return new ResponseEntity<>(er, httpStatus);
    }

    @ExceptionHandler(EntityExistsException.class)
    public ResponseEntity<ExceptionResponse> handleExistsException(EntityExistsException e, HttpServletRequest req) {
        HttpStatus httpStatus = HttpStatus.CONFLICT;
        ExceptionResponse er = new ExceptionResponse(
                Instant.now(),
                httpStatus.value(),
                e.getMessage(),
                req.getServletPath()
        );
        return new ResponseEntity<>(er, httpStatus);
    }
}
