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

    @PatchMapping(path = "/api/v1/users/{username}")
    public ResponseEntity<UserProfileResponse> partialUpdate(@PathVariable String username, @Valid @RequestBody UpdateUserRequest dto) {
        UserProfileResponse upr = userService.partialUpdate(username, dto);
        return new ResponseEntity<>(upr, HttpStatus.OK);
    }

    @PatchMapping(path = "/api/v1/users/{username}/password")
    public ResponseEntity<GenericResponse> changePassword(@PathVariable String username, @Valid @RequestBody ChangePasswordRequest dto) {
        GenericResponse gr = userService.changePassword(username, dto);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }
}
