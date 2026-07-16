package com.blogforge.controller;

import com.blogforge.dto.AuthorProfileResponse;
import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.user.UserSummaryResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.service.AuthorService;
import com.blogforge.service.FollowService;
import com.blogforge.specification.user.UserSpecificationParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authors", description = "Endpoints for author profiles and listings")
@RestController
public class AuthorController {

    private final AuthorService authorService;
    private final FollowService followService;

    public AuthorController(
            AuthorService authorService,
            FollowService followService) {
        this.authorService = authorService;
        this.followService = followService;
    }

    @Operation(summary = "Get all authors", description = "Returns a paginated list of author summaries. Supports filtering via query parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of authors returned successfully")
    })
    @GetMapping(path = "/api/v1/authors")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute UserSpecificationParams specParams
    ) {
        PagedResponse<UserSummaryResponse> upr = authorService.getAllAuthorSummary(reqParams, specParams);
        return new ResponseEntity<>(upr, HttpStatus.OK);
    }

    @Operation(summary = "Get author profile by username", description = "Fetches the profile of an author, indicating if the current user is following them.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author profile returned successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
    })
    @GetMapping(path = "/api/v1/authors/{username:.+}")
    public ResponseEntity<AuthorProfileResponse> getAuthorProfile(
            @Parameter(description = "The username of the author") @PathVariable String username,
            @AuthenticationPrincipal UserDetails principal) {

        String authenticatedUsername = (principal != null)
                ? principal.getUsername()
                : null;

        AuthorProfileResponse apr = authorService.getAuthorProfile(username, authenticatedUsername);
        return new ResponseEntity<>(apr, HttpStatus.OK);
    }

    @Operation(summary = "Get my author profile", description = "Returns the author profile of the currently authenticated user.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author profile returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Author profile not found for the user", content = @Content)
    })
    @GetMapping(path = "/api/v1/authors/me")
    public ResponseEntity<AuthorProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails principal) {
        String currentUserUsername = (principal != null)
                ? principal.getUsername() : null;
        AuthorProfileResponse myProfile = authorService.getMyProfile(currentUserUsername);
        return new ResponseEntity<>(myProfile, HttpStatus.OK);
    }

    @Tag(name = "Follows", description = "Endpoints for following and unfollowing authors")
    @Operation(summary = "Follow an author", description = "Follows the specified author.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successfully followed author"),
            @ApiResponse(responseCode = "400", description = "Cannot follow yourself or already following", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
    })
    @PostMapping(path = "/api/v1/authors/{username:.+}/follow")
    public ResponseEntity<GenericResponse> followAuthor(
            @Parameter(description = "The username of the author to follow") @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails principal) {
        GenericResponse gr = followService.create(username, principal.getUsername());
        return new ResponseEntity<>(gr, HttpStatus.CREATED);
    }

    @Tag(name = "Follows")
    @Operation(summary = "Unfollow an author", description = "Unfollows the specified author.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Successfully unfollowed author"), // the controller returns CREATED (201) currently, let's keep it as is
            @ApiResponse(responseCode = "400", description = "Not following author", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content)
    })
    @DeleteMapping(path = "/api/v1/authors/{username:.+}/follow")
    public ResponseEntity<GenericResponse> unfollowAuthor(
            @Parameter(description = "The username of the author to unfollow") @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails principal) {
        GenericResponse gr = followService.delete(username, principal.getUsername());
        return new ResponseEntity<>(gr, HttpStatus.CREATED);
    }
}
