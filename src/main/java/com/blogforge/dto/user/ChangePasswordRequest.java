package com.blogforge.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        String oldPassword,

        @NotBlank(message = "{user.password.notBlank}")
        @Size(min = 8, message = "{user.password.size}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).*$",
                message = "{user.password.pattern}"
        )
        String newPassword,

        @NotBlank(message = "{user.password.notBlank}")
        @Size(min = 8, message = "{user.password.size}")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).*$",
                message = "{user.password.pattern}"
        )
        String confirmNewPassword
) {
}
