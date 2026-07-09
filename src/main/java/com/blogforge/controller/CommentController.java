package com.blogforge.controller;

import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.CommentService;
import com.blogforge.specification.comment.CommentSpecificationParams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping(path = "/api/v1/comments")
    public ResponseEntity<PagedResponse<CommentResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute CommentSpecificationParams specParams
            ) {
        PagedResponse<CommentResponse> comments = commentService.getAll(reqParams, specParams);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }
}
