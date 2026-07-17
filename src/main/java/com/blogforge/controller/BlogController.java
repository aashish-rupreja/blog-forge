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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

import java.util.List;
import java.util.UUID;

@Tag(name = "Blogs", description = "Endpoints for creating, retrieving, updating, and deleting blog posts")
@RestController
public class BlogController {

    private static final Logger LOG = LoggerFactory.getLogger(BlogController.class);

    private final BlogService blogService;
    private final CommentService commentService;

    public BlogController(BlogService blogService, CommentService commentService) {
        this.blogService = blogService;
        this.commentService = commentService;
    }

    @Operation(summary = "Get all blogs", description = "Returns a paginated list of blog summaries. Supports filtering/searching via parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of blogs returned successfully")
    })
    @GetMapping(path = "/api/v1/blogs")
    public ResponseEntity<PagedResponse<BlogSummaryResponse>> getAllSummary(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute BlogSpecificationParams specParams
    ) {
        LOG.trace("Entering getAllSummary with reqParams: {}, specParams: {}", reqParams, specParams);
        PagedResponse<BlogSummaryResponse> blogs = blogService.getAllSummary(reqParams, specParams);
        LOG.trace("Exiting getAllSummary with response count: {}", blogs.getContent() != null ? blogs.getContent().size() : 0);
        return new ResponseEntity<>(blogs, HttpStatus.OK);
    }

    @Operation(summary = "Get blog details by slug", description = "Fetches complete details of a blog post by its unique URL slug.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog details returned successfully"),
            @ApiResponse(responseCode = "404", description = "Blog not found", content = @Content)
    })
    @GetMapping("/api/v1/blogs/{slug}")
    public ResponseEntity<BlogDetailsResponse> getBlogDetails(
            @Parameter(description = "The unique slug of the blog post") @PathVariable String slug) {
        LOG.trace("Entering getBlogDetails with slug: {}", slug);
        BlogDetailsResponse bdr = blogService.getBlogDetails(slug);
        LOG.trace("Exiting getBlogDetails with response: {}", bdr);
        return new ResponseEntity<>(bdr, HttpStatus.OK);
    }

    @Operation(summary = "Get comments of a blog", description = "Returns a paginated list of comments for the specified blog slug.",
            tags = {"Comments"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of comments returned successfully"),
            @ApiResponse(responseCode = "404", description = "Blog not found", content = @Content)
    })
    @GetMapping("/api/v1/blogs/{slug}/comments")
    public ResponseEntity<PagedResponse<CommentResponse>> getBlogComments(
            @Parameter(description = "The unique slug of the blog post") @PathVariable String slug,
            @ModelAttribute PaginationRequestParams reqParams
    ) {
        LOG.trace("Entering getBlogComments with slug: {}, reqParams: {}", slug, reqParams);
        PagedResponse<CommentResponse> comments = blogService.getBlogComments(slug, reqParams);
        LOG.trace("Exiting getBlogComments with response count: {}", comments.getContent() != null ? comments.getContent().size() : 0);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @Operation(summary = "Partially update a blog", description = "Updates specific fields of an existing blog post. Only the author can perform this action.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - you are not the author of this blog", content = @Content),
            @ApiResponse(responseCode = "404", description = "Blog not found", content = @Content)
    })
    // following operations are only allowed to the author of the blog, authorization will be added later
    @PatchMapping(path = "/api/v1/blogs/{slug}")
    public ResponseEntity<BlogDetailsResponse> partialUpdate(
            @Parameter(description = "The unique slug of the blog post") @PathVariable String slug,
            @RequestBody UpdateBlogRequest updateBlogRequest,
            @AuthenticationPrincipal CustomUserDetails principal) {
        LOG.trace("Entering partialUpdate with slug: {}, updateBlogRequest: {}, principal: {}", slug, updateBlogRequest, principal != null ? principal.getUsername() : null);
        BlogDetailsResponse bdr = blogService.partialUpdate(slug, updateBlogRequest, principal);
        LOG.trace("Exiting partialUpdate with response: {}", bdr);
        return new ResponseEntity<>(bdr, HttpStatus.OK);
    }

    @Operation(summary = "Delete a blog by slug", description = "Performs a soft delete on a blog post.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blog deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden", content = @Content),
            @ApiResponse(responseCode = "404", description = "Blog not found", content = @Content)
    })
    @DeleteMapping(path = "/api/v1/blogs/{slug}")
    public ResponseEntity<GenericResponse> delete(
            @Parameter(description = "The unique slug of the blog post") @PathVariable String slug) {
        LOG.trace("Entering delete with slug: {}", slug);
        GenericResponse gr = blogService.delete(slug);
        LOG.trace("Exiting delete with response: {}", gr);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }

    @Operation(summary = "Get my blog posts", description = "Returns a paginated list of blog posts owned by the authenticated user.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of user's blogs returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping(path = "/api/v1/blogs/own")
    public ResponseEntity<PagedResponse<BlogSummaryResponse>> getMyBlogs(
            @ModelAttribute PaginationRequestParams reqParams,
            @AuthenticationPrincipal CustomUserDetails principal) {
        LOG.trace("Entering getMyBlogs with reqParams: {}, principal: {}", reqParams, principal != null ? principal.getUsername() : null);
        String username = (principal != null) ? principal.getUsername() : null;
        PagedResponse<BlogSummaryResponse> myBlogs = blogService.getMyBlogs(reqParams, username);
        LOG.trace("Exiting getMyBlogs with response count: {}", myBlogs.getContent() != null ? myBlogs.getContent().size() : 0);
        return new ResponseEntity<>(myBlogs, HttpStatus.OK);
    }

    @Operation(summary = "Create a new blog", description = "Creates a new blog post for the authenticated author.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Blog created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - only authors can create blogs", content = @Content)
    })
    @PostMapping(path = "/api/v1/blogs")
    public ResponseEntity<BlogDetailsResponse> create(
            @Valid @RequestBody CreateBlogRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        LOG.trace("Entering create with dto: {}, principal: {}", dto, principal != null ? principal.getUsername() : null);
        String username = (principal != null) ? principal.getUsername() : null;
        BlogDetailsResponse blogResponse = blogService.create(dto, username);
        LOG.trace("Exiting create with response: {}", blogResponse);
        return new ResponseEntity<>(blogResponse, HttpStatus.CREATED);
    }

    @Operation(summary = "Add a comment to a blog", description = "Adds a comment to the specified blog post.",
            security = @SecurityRequirement(name = "Bearer Authentication"),
            tags = {"Comments"})
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comment added successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Blog not found", content = @Content)
    })
    @PostMapping(path = "/api/v1/blogs/{slug}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @Parameter(description = "The unique slug of the blog post") @PathVariable String slug,
            @Valid @RequestBody CreateCommentRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        LOG.trace("Entering addComment with slug: {}, dto: {}, principal: {}", slug, dto, principal != null ? principal.getUsername() : null);
        CommentResponse comment = commentService.addComment(slug, dto, principal.getUsername());
        LOG.trace("Exiting addComment with response: {}", comment);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @Operation(summary = "Hard delete blogs (Admin only)", description = "Permanently deletes list of blogs specified by UUIDs.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blogs permanently deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content)
    })
    @DeleteMapping(path = "/api/v1/blogs/hard-delete")
    public ResponseEntity<GenericResponse> hardDelete(
            @Parameter(description = "List of UUIDs of blogs to hard delete") @RequestBody List<UUID> uuids) {
        LOG.trace("Entering hardDelete with uuids: {}", uuids);
        GenericResponse gr = blogService.hardDelete(uuids);
        LOG.trace("Exiting hardDelete with response: {}", gr);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }

    @Operation(summary = "Like or react to a blog", description = "Likes/unlikes or registers a reaction on a blog post.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reaction registered successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Blog not found", content = @Content)
    })
    @PostMapping("/api/v1/blogs/{slug}/like")
    public ResponseEntity<GenericResponse> like(
            @Parameter(description = "The unique slug of the blog post") @PathVariable String slug,
            @RequestBody AddReactionRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        LOG.trace("Entering like with slug: {}, dto: {}, principal: {}", slug, dto, principal != null ? principal.getUsername() : null);
        GenericResponse gr = blogService.like(slug, dto, principal);
        LOG.trace("Exiting like with response: {}", gr);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }

}
