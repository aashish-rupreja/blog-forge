package com.blogforge.dto.authorapplication;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAuthorApplicationRequest(
        @NotBlank(message = "{authorApplication.applicationReason.blank}")
        @Size(min = 100, max = 500, message = "{authorApplication.applicationReason.size}")
        String applicationReason
) {
}
