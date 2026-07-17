package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.dto.comment.UpdateCommentRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.service.CommentService;
import com.blogforge.specification.comment.CommentSpecificationParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Comments", description = "Endpoints for managing comments on blog posts")
@RestController
public class CommentController {

    private static final Logger LOG = LoggerFactory.getLogger(CommentController.class);

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Get all comments", description = "Returns a paginated list of comments. Supports filtering/searching via parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of comments returned successfully")
    })
    @GetMapping(path = "/api/v1/comments")
    public ResponseEntity<PagedResponse<CommentResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute CommentSpecificationParams specParams
            ) {
        LOG.trace("Entering getAll with reqParams: {}, specParams: {}", reqParams, specParams);
        PagedResponse<CommentResponse> comments = commentService.getAll(reqParams, specParams);
        LOG.trace("Exiting getAll with response count: {}", comments.getContent() != null ? comments.getContent().size() : 0);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @Operation(summary = "Partially update a comment", description = "Updates specific fields of an existing comment. Only the comment author can perform this action.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - you are not the author of this comment", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
    })
    @PatchMapping(path = "/api/v1/comments/{id}")
    public ResponseEntity<CommentResponse> partialUpdateComment(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody UpdateCommentRequest dto,
            @Parameter(description = "The UUID of the comment") @PathVariable UUID id) {
        LOG.trace("Entering partialUpdateComment with id: {}, dto: {}, principal: {}", id, dto, principal != null ? principal.getUsername() : null);
        CommentResponse updated = commentService.partialUpdate(id, dto, principal.getUsername());
        LOG.trace("Exiting partialUpdateComment with response: {}", updated);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @Operation(summary = "Delete a comment", description = "Deletes a comment. Authorized user must be the author of the comment or the author of the blog post.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - no permission to delete this comment", content = @Content),
            @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content)
    })
    @DeleteMapping(path = "/api/v1/comments/{id}")
    public ResponseEntity<GenericResponse> deleteComment(
            @Parameter(description = "The UUID of the comment") @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails principal
    ) {
        LOG.trace("Entering deleteComment with id: {}, principal: {}", id, principal != null ? principal.getUsername() : null);
        GenericResponse deleteResponse = commentService.delete(id, principal);
        LOG.trace("Exiting deleteComment with response: {}", deleteResponse);
        return new ResponseEntity<>(deleteResponse, HttpStatus.OK);
    }

}
