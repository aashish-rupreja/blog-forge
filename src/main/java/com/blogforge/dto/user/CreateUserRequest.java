package com.blogforge.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "{user.firstName.notBlank}")
        @Size(min = 3, max = 20, message = "{user.firstName.size}")
        String firstName,

        @Size(max = 20, message = "{user.lastName.size}")
        String lastName,

        @NotBlank(message = "{user.username.notBlank}")
        @Size(min = 3, max = 10, message = "{user.username.size}")
        @Pattern(
                regexp = "^[A-Za-z0-9_.]+$",
                message = "{user.username.pattern}"
        )
        String username,

        @Size(max = 100, message = "{user.profilePicLink.size}")
        String profilePicLink,

        @Size(max = 255, message = "{user.bio.size}")
        String bio,

        @NotBlank(message = "{user.email.notBlank}")
        @Email(message = "{user.email.invalid}")
        String email,

        @NotBlank(message = "{user.password.notBlank}")
        @Size(min = 8, message = "{user.password.size}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).*$",
                message = "{user.password.pattern}"
        )
        String password
) {
}