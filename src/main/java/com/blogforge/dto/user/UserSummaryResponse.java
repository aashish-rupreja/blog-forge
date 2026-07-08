package com.blogforge.dto.user;

import com.blogforge.dto.BaseResponse;
import com.blogforge.entity.UserStatus;

import java.util.Set;

public record UserSummaryResponse(
        BaseResponse baseResponse,
        String firstName,
        String lastName,
        String username,
        String profilePicLink,
        String email,
        UserStatus status,
        Set<String> roles,
        long blogCount,
        long commentCount,
        long reactionCount
) {
}
