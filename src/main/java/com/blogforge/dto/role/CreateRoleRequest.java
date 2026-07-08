package com.blogforge.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank(message = "{role.name.blank}")
        @Size(max = 20, message = "{role.name.size}")
        String name
) {
}
