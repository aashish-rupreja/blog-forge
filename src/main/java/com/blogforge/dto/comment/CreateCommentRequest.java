package com.blogforge.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "{comment.content.notBlank}")
        @Size(max = 500, message = "{comment.content.size}")
        String content
) {
}
