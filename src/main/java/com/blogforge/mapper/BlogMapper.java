package com.blogforge.mapper;

import com.blogforge.dto.BaseResponse;
import com.blogforge.dto.blog.*;
import com.blogforge.entity.Blog;
import com.blogforge.entity.Category;
import com.blogforge.entity.ReactionType;
import com.blogforge.entity.Tag;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Component
public class BlogMapper {

    public Blog fromCreateRequestToEntity(CreateBlogRequest dto) {
        Blog b = new Blog();
        b.setTitle(dto.title());
        b.setContent(dto.content());
        b.setEnableComments(dto.enableComments());
        b.setStatus(dto.blogStatus());
        // set categories and tags in service layer as it requires db lookup
        return b;
    }

    public BlogDetailsResponse fromEntityToDetailsResponse(Blog b) {
        return new BlogDetailsResponse(
                new BaseResponse(
                        b.getUuid(),
                        b.getCreatedAt(),
                        b.getUpdatedAt()),
                b.getTitle(),
                b.getSlug(),
                b.getContent(),
                b.getCategories().stream().map(Category::getName).collect(Collectors.toSet()),
                b.getTags().stream().map(Tag::getName).collect(Collectors.toSet()),
                b.getPublishedAt(),
                b.getReactions().stream().filter(r -> r.getReactionType().equals(ReactionType.LIKE)).count(),
                b.getReactions().stream().filter(r -> r.getReactionType().equals(ReactionType.DISLIKE)).count(),
                b.isEnableComments(),
                b.getComments().size()
        );
    }

    public BlogSummaryResponse fromEntityToSummaryResponse(Blog b) {
        return new BlogSummaryResponse(
                new BaseResponse(
                        b.getUuid(),
                        b.getCreatedAt(),
                        b.getUpdatedAt()),
                b.getTitle(),
                b.getAuthor().getUsername(),
                b.getSlug(),
                b.getCategories().stream().map(Category::getName).collect(Collectors.toSet()),
                b.getTags().stream().map(Tag::getName).collect(Collectors.toSet()),
                b.getPublishedAt(),
                b.getReactions().stream().filter(r -> r.equals(ReactionType.LIKE)).count(),
                b.getReactions().stream().filter(r -> r.equals(ReactionType.DISLIKE)).count(),
                b.isEnableComments(),
                b.getComments().size()
        );
    }
}
