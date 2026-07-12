package com.blogforge.dto.blog;

import com.blogforge.entity.BlogStatus;
import jakarta.validation.constraints.*;

import java.util.Set;

public record UpdateBlogRequest(
        @Size(min = 5, max = 50, message = "{blog.title.size}")
        String title,

        @Size(min = 100, max = 1000, message = "{blog.content.size}")
        String content,

        Boolean enableComments,

        BlogStatus blogStatus,

        @Size(min = 1, max = 5, message = "{blog.categoryIds.size}")
        Set<String> categoryIds,

        @Size(max = 10, message = "{blog.tags.size}")
        Set<String> tags
) {
}
