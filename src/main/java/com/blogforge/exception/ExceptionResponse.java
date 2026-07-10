package com.blogforge.exception;

import java.time.Instant;

public record ExceptionResponse(
        Instant timestamp,
        int status,
        String error,
        String path
) {
}
