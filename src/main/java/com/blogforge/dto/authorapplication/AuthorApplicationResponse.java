package com.blogforge.dto.authorapplication;

import com.blogforge.dto.BaseResponse;
import com.blogforge.entity.AuthorApplicationStatus;

import java.time.Instant;

public record AuthorApplicationResponse(
    BaseResponse commonFields,
    String applicantUsername,
    String applicationReviewerUsername,
    AuthorApplicationStatus status,
    String reviewerRemarks,
    Instant reviewedAt
) {
}
