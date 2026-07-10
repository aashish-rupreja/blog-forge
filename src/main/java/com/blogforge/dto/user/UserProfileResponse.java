package com.blogforge.dto.user;

import com.blogforge.dto.BaseResponse;

import java.time.LocalDate;

public record UserProfileResponse(
        BaseResponse commonFields,
        String firstName,
        String lastName,
        String username,
        String profilePicLink,
        String bio,
        long blogCount
) {
}
