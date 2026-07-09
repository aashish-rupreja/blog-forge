package com.blogforge.specification.authorapplication;

import com.blogforge.entity.AuthorApplication;
import com.blogforge.entity.AuthorApplicationStatus;

import java.util.UUID;

public record AuthorApplicationSpecificationParams(
        String applicantUsername,
        String applicationReviewerUsername,
        AuthorApplicationStatus status
) {
}
