package com.blogforge.dto.authorapplication;

import com.blogforge.entity.AuthorApplicationStatus;

public record MyAuthorApplicationsRequest(
        AuthorApplicationStatus status,
        String reviewedBeforeDate,
        String reviewedOnDate,
        String reviewedAfterDate
) {
}
