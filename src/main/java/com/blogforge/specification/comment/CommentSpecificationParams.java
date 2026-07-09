package com.blogforge.specification.comment;

public record CommentSpecificationParams(
        String owner,
        String content,
        String postedAfter,
        String postedBefore,
        String postedOn
) {
}
