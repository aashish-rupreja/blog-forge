package com.blogforge.dto.user;

import com.blogforge.dto.BaseResponse;

import java.time.LocalDate;

public record UserProfileResponse(
        BaseResponse commonFields,
        String firstName,
        String lastName,
        String username,
        String email,
        String profilePicLink,
        String bio
) {
}
