package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.user.UserProfileResponse;
import com.blogforge.dto.user.UserSummaryResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.AuthorService;
import com.blogforge.service.FollowService;
import com.blogforge.specification.user.UserSpecificationParams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<UserProfileResponse> getAuthorProfile(@PathVariable String username) {
        UserProfileResponse upr = authorService.getAuthorProfile(username);
        return new ResponseEntity<>(upr, HttpStatus.OK);
    }

    @PostMapping(path = "/api/v1/authors/{authorName}/follow")
    public ResponseEntity<GenericResponse> followAuthor(@PathVariable String authorName) {
        GenericResponse gr = followService.create(authorName);
        return new ResponseEntity<>(gr, HttpStatus.CREATED);
    }
}
