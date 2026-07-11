package com.blogforge.dto.role;

import com.blogforge.entity.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateRoleRequest(
        @NotBlank(message = "{role.name.blank}")
        @Size(max = 20, message = "{role.name.size}")
        @Pattern(
                regexp = "^[A-Za-z]+$",
                message = "{role.name.pattern}"
        )
        String name,

        @NotBlank(message = "{role.type.blank}")
        RoleType roleType
) {
}
