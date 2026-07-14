package com.blogforge.dto;

public record AuthorProfileResponse(
        BaseResponse commonFields,
        String firstName,
        String lastName,
        String username,
        String profilePicLink,
        String bio,
        int blogCount,
        boolean amIFollowingThisAuthor
) {
}
