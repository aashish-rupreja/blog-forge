package com.blogforge.dto.comment;

import com.blogforge.dto.BaseResponse;

import java.time.LocalDate;

public record CommentResponse(
        BaseResponse baseResponse,
        String owner,
        String profilePicLink,
        String blogSlug
) {
}
