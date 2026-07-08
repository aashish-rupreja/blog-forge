package com.blogforge.dto.blog;

import com.blogforge.dto.BaseResponse;

import java.time.LocalDate;
import java.util.Set;

public record BlogDetailsResponse(
        BaseResponse baseResponse,
        String title,
        String slug,
        String content,
        Set<String> categories,
        Set<String> tags,
        LocalDate publishedOn,
        long likeCount,
        long dislikeCount,
        boolean commentsEnabled,
        long commentCount
) {
}
