package com.blogforge.dto.blog;

import com.blogforge.dto.BaseResponse;

import java.time.Instant;
import java.util.Set;

public record BlogSummaryResponse(
        BaseResponse commonFields,
        String title,
        String authorUsername,
        String slug,
        Set<String> categories,
        Set<String> tags,
        Instant publishedOn,
        long likeCount,
        long dislikeCount,
        boolean commentsEnabled,
        long commentCount,
        String authorProfilePicLink,
        com.blogforge.entity.BlogStatus blogStatus
) {
}
