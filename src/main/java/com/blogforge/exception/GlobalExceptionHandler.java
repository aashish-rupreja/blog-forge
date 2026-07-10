package com.blogforge.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
}
