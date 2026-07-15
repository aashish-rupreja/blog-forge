package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.dto.comment.UpdateCommentRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.service.CommentService;
import com.blogforge.specification.comment.CommentSpecificationParams;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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

    @PatchMapping(path = "/api/v1/comments/{id}")
    public ResponseEntity<CommentResponse> partialUpdateComment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateCommentRequest dto,
            @PathVariable UUID id) {
        CommentResponse updated = commentService.partialUpdate(id, dto, principal.getUsername());
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @DeleteMapping(path = "/api/v1/comments/{id}")
    public ResponseEntity<GenericResponse> deleteComment(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        GenericResponse deleteResponse = commentService.delete(id, principal);
        return new ResponseEntity<>(deleteResponse, HttpStatus.OK);
    }

}
