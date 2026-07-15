package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.blog.BlogDetailsResponse;
import com.blogforge.dto.blog.BlogSummaryResponse;
import com.blogforge.dto.blog.CreateBlogRequest;
import com.blogforge.dto.blog.UpdateBlogRequest;
import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.dto.comment.CreateCommentRequest;
import com.blogforge.dto.reaction.AddReactionRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.service.BlogService;
import com.blogforge.service.CommentService;
import com.blogforge.specification.blog.BlogSpecificationParams;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class BlogController {

    private final BlogService blogService;
    private final CommentService commentService;

    public BlogController(BlogService blogService, CommentService commentService) {
        this.blogService = blogService;
        this.commentService = commentService;
    }

    @GetMapping(path = "/api/v1/blogs")
    public ResponseEntity<PagedResponse<BlogSummaryResponse>> getAllSummary(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute BlogSpecificationParams specParams
            ) {

        PagedResponse<BlogSummaryResponse> blogs = blogService.getAllSummary(reqParams, specParams);
        return new ResponseEntity<>(blogs, HttpStatus.OK);

    }

    @GetMapping("/api/v1/blogs/{slug}")
    public ResponseEntity<BlogDetailsResponse> getBlogDetails(@PathVariable String slug) {
        BlogDetailsResponse bdr = blogService.getBlogDetails(slug);
        return new ResponseEntity<>(bdr, HttpStatus.OK);
    }

    @GetMapping("/api/v1/blogs/{slug}/comments")
    public ResponseEntity<PagedResponse<CommentResponse>> getBlogComments(
            @PathVariable String slug,
            @ModelAttribute PaginationRequestParams reqParams
            ) {
        PagedResponse<CommentResponse> comments = blogService.getBlogComments(slug, reqParams);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    // following operations are only allowed to the author of the blog, authorization will be added later
    @PatchMapping(path = "/api/v1/blogs/{slug}")
    public ResponseEntity<BlogDetailsResponse> partialUpdate(@PathVariable String slug, @RequestBody UpdateBlogRequest updateBlogRequest) {
        BlogDetailsResponse bdr = blogService.partialUpdate(slug, updateBlogRequest);
        return new ResponseEntity<>(bdr, HttpStatus.OK);
    }

    @DeleteMapping(path = "/api/v1/blogs/{slug}")
    public ResponseEntity<GenericResponse> delete(@PathVariable String slug) {
        GenericResponse gr = blogService.delete(slug);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }

    @GetMapping(path = "/api/v1/blogs/own")
    public ResponseEntity<PagedResponse<BlogSummaryResponse>> getMyBlogs(@ModelAttribute PaginationRequestParams reqParams) {
        PagedResponse<BlogSummaryResponse> myBlogs = blogService.getMyBlogs(reqParams);
        return new ResponseEntity<>(myBlogs, HttpStatus.OK);
    }

    @PostMapping(path = "/api/v1/blogs")
    public ResponseEntity<BlogDetailsResponse> create(@Valid @RequestBody CreateBlogRequest dto) {
        BlogDetailsResponse blogResponse = blogService.create(dto);
        return new ResponseEntity<>(blogResponse, HttpStatus.CREATED);
    }

    @PostMapping(path = "/api/v1/blogs/{slug}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String slug,
            @Valid @RequestBody CreateCommentRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal) {

        CommentResponse comment = commentService.addComment(slug, dto, principal.getUser());
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/api/v1/blogs/hard-delete")
    public ResponseEntity<GenericResponse> hardDelete(@RequestBody List<UUID> uuids) {
        GenericResponse gr = blogService.hardDelete(uuids);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }

    @PostMapping("/api/v1/blogs/{slug}/like")
    public ResponseEntity<GenericResponse> like(
            @PathVariable String slug,
            @RequestBody AddReactionRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        GenericResponse gr = blogService.like(slug, dto, principal);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }

}
