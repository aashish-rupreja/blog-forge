package com.blogforge.specification.blog;

import java.util.Set;

public record BlogSpecificationParams(
        String title,
        String authorName,
        Set<String> categories,
        Set<String> tags,
        String publishedAfter,
        String publishedBefore,
        String publishedOn
) {
}
