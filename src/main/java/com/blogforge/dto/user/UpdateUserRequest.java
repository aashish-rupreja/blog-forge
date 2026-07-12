package com.blogforge.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 3, max = 20, message = "{user.firstName.size}")
        String firstName,

        @Size(max = 20, message = "{user.lastName.size}")
        String lastName,

        @Size(min = 3, max = 10, message = "{user.username.size}")
        @Pattern(
                regexp = "^[A-Za-z0-9_]+$",
                message = "{user.username.pattern}"
        )
        String username,

        @Size(max = 100, message = "{user.profilePicLink.size}")
        String profilePicLink,

        @Size(max = 255, message = "{user.bio.size}")
        String bio
) {
}
