package com.blogforge.service;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.dto.comment.CreateCommentRequest;
import com.blogforge.dto.comment.UpdateCommentRequest;
import com.blogforge.entity.User;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.specification.comment.CommentSpecificationParams;

import java.util.UUID;

public interface CommentService {

    PagedResponse<CommentResponse> getAll(PaginationRequestParams reqParams, CommentSpecificationParams specParams);

    CommentResponse addComment(
            String blogSlug,
            CreateCommentRequest dto,
            String commentorUsername
    );

    CommentResponse partialUpdate(
            UUID commentId,
            UpdateCommentRequest dto,
            String commentOwnerUsername);

    GenericResponse delete(UUID id, CustomUserDetails principal);
}
