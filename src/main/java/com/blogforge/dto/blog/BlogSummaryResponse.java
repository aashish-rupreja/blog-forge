package com.blogforge.dto.blog;

import com.blogforge.dto.BaseResponse;

import java.time.LocalDate;
import java.util.Set;

public record BlogSummaryResponse(
        BaseResponse commonFields,
        String title,
        String slug,
        Set<String> categories,
        Set<String> tags,
        LocalDate publishedOn,
        long likeCount,
        long dislikeCount,
        boolean commentsEnabled,
        long commentCount
) {
}
