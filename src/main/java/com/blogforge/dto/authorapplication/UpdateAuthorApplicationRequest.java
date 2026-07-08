package com.blogforge.dto.authorapplication;

import com.blogforge.entity.AuthorApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAuthorApplicationRequest(

        @NotBlank(message = "{authorApplication.status.blank}")
        AuthorApplicationStatus status,

        @NotBlank(message = "{authorApplication.reviewerRemarks.blank}")
        @Size(max = 100, message = "{authorApplication.reviewerRemarks.size}")
        String reviewerRemarks
) {
}
