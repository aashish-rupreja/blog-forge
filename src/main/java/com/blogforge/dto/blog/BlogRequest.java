package com.blogforge.dto.blog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record BlogRequest(
        @NotBlank(message = "{blog.title.notBlank}")
        @Size(min = 5, max = 50, message = "{blog.title.size}")
        String title,

        @NotBlank(message = "{blog.content.notBlank}")
        @Size(min = 100, max = 1000, message = "{blog.content.size}")
        String content,

        @NotNull(message = "{blog.enableComments.notNull}")
        boolean enableComments,

        @NotBlank(message = "{blog.blogStatus.notBlank}")
        String blogStatus,

        @NotEmpty(message = "{blog.categoryIds.notEmpty}")
        @Size(min = 1, max = 5, message = "{blog.categoryIds.size}")
        Set<String> categoryIds,

        @Size(max = 10, message = "{blog.tags.size}")
        Set<String> tags
) {
}
