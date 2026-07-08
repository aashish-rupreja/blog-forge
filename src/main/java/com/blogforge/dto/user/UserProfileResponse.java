package com.blogforge.dto.user;

import java.time.LocalDate;

public record UserProfileResponse(
        String firstName,
        String lastName,
        String username,
        String profilePicLink,
        String bio,
        LocalDate joinedOn
) {
}
