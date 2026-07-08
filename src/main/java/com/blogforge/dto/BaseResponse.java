package com.blogforge.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BaseResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt
) {
}
