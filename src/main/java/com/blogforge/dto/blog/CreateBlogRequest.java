package com.blogforge.dto.blog;

import com.blogforge.entity.BlogStatus;
import jakarta.validation.constraints.*;

import java.util.Set;

public record CreateBlogRequest(
        @NotBlank(message = "{blog.title.notBlank}")
        @Size(min = 5, max = 50, message = "{blog.title.size}")
        String title,

        @NotBlank(message = "{blog.content.notBlank}")
        @Size(min = 100, max = 5000, message = "{blog.content.size}")
        String content,

        @NotNull(message = "{blog.enableComments.notNull}")
        boolean enableComments,

        @NotBlank(message = "{blog.blogStatus.notBlank}")
        BlogStatus blogStatus,

        @NotEmpty(message = "{blog.categoryIds.notEmpty}")
        @Size(min = 1, max = 5, message = "{blog.categoryIds.size}")
        Set<String> categories,

        @Size(max = 10, message = "{blog.tags.size}")
        Set<String> tags
) {
}
