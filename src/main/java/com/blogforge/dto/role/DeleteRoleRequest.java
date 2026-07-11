package com.blogforge.dto.role;

import jakarta.validation.constraints.*;

import java.util.Set;

public record DeleteRoleRequest(
        @NotNull
        @NotEmpty
        Set<String> roles
) {
}
