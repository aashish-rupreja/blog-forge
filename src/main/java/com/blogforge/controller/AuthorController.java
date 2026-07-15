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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(path = "/api/v1/authors")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute UserSpecificationParams specParams
    ) {
        PagedResponse<UserSummaryResponse> upr = authorService.getAllAuthorSummary(reqParams, specParams);
        return new ResponseEntity<>(upr, HttpStatus.OK);
    }

    @GetMapping(path = "/api/v1/authors/{username}")
    public ResponseEntity<AuthorProfileResponse> getAuthorProfile(
            @PathVariable String username,
            @AuthenticationPrincipal UserDetails principal) {

        String authenticatedUsername = (principal != null)
                ? principal.getUsername()
                : null;

        AuthorProfileResponse apr = authorService.getAuthorProfile(username, authenticatedUsername);
        return new ResponseEntity<>(apr, HttpStatus.OK);
    }

    @GetMapping(path = "/api/v1/authors/me")
    public ResponseEntity<AuthorProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails principal) {
        String currentUserUsername = (principal != null)
                ? principal.getUsername() : null;
        AuthorProfileResponse myProfile = authorService.getMyProfile(currentUserUsername);
        return new ResponseEntity<>(myProfile, HttpStatus.OK);
    }

    @PostMapping(path = "/api/v1/authors/{username}/follow")
    public ResponseEntity<GenericResponse> followAuthor(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails principal) {
        GenericResponse gr = followService.create(username, principal.getUsername());
        return new ResponseEntity<>(gr, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/api/v1/authors/{username}/follow")
    public ResponseEntity<GenericResponse> unfollowAuthor(
            @PathVariable String username,
            @AuthenticationPrincipal CustomUserDetails principal) {
        GenericResponse gr = followService.delete(username, principal.getUsername());
        return new ResponseEntity<>(gr, HttpStatus.CREATED);
    }
}
