package com.blogforge.mapper;

import com.blogforge.dto.BaseResponse;
import com.blogforge.dto.comment.*;
import com.blogforge.entity.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public Comment fromCreateRequestToEntity(CreateCommentRequest dto) {
        Comment c = new Comment();
        c.setContent(dto.content());
        return c;
    }

    public CommentResponse fromEntityToResponse(Comment c) {
        return new CommentResponse(
                new BaseResponse(
                        c.getUuid(),
                        c.getCreatedAt(),
                        c.getUpdatedAt()),
                c.getOwner().getUsername(),
                c.getOwner().getProfilePicLink(),
                c.getBlog().getSlug()
        );
    }
}
