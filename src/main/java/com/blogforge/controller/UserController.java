package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.user.*;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.UserService;
import com.blogforge.specification.user.UserSpecificationParams;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/v1/users")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getAllUserSummary(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute UserSpecificationParams specParams
    ) {
        PagedResponse<UserSummaryResponse> usr = userService.getAllUserSummary(reqParams, specParams);
        return new ResponseEntity<>(usr, HttpStatus.OK);
    }

    @GetMapping(path = "/api/v1/users/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String username) {
        UserProfileResponse upr = userService.getUserProfile(username);
        return new ResponseEntity<>(upr, HttpStatus.OK);
    }

    @PostMapping(path = "/api/v1/users")
    public ResponseEntity<UserProfileResponse> create(@Valid @RequestBody CreateUserRequest dto) {
        UserProfileResponse upr = userService.create(dto);
        return new ResponseEntity<>(upr, HttpStatus.CREATED);
    }

    @PatchMapping(path = "/api/v1/users/me/profile")
    public ResponseEntity<UserProfileResponse> partialUpdate(@Valid @RequestBody UpdateUserRequest dto) {
        UserProfileResponse upr = userService.partialUpdate(dto);
        return new ResponseEntity<>(upr, HttpStatus.OK);
    }

    @PatchMapping(path = "/api/v1/users/me/password")
    public ResponseEntity<GenericResponse> changePassword(@Valid @RequestBody ChangePasswordRequest dto) {
        GenericResponse gr = userService.changePassword(dto);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }

    @GetMapping(path = "/api/v1/users/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails principal) {
        String currentUserUsername = (principal != null)
                ? principal.getUsername() : null;
        UserProfileResponse myProfile = userService.getUserProfile(currentUserUsername);
        return new ResponseEntity<>(myProfile, HttpStatus.OK);
    }
}
