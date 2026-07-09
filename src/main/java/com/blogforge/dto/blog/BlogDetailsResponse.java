package com.blogforge.dto.blog;

import com.blogforge.dto.BaseResponse;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

public record BlogDetailsResponse(
        BaseResponse commonFields,
        String title,
        String slug,
        String content,
        Set<String> categories,
        Set<String> tags,
        Instant publishedOn,
        long likeCount,
        long dislikeCount,
        boolean commentsEnabled,
        long commentCount
) {
}
