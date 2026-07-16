package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.user.*;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.service.UserService;
import com.blogforge.specification.user.UserSpecificationParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "Endpoints for user registration, profile management, and account operations")
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users", description = "Returns a paginated list of user summaries. Supports filtering via query parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of users returned successfully")
    })
    @GetMapping("/api/v1/users")
    public ResponseEntity<PagedResponse<UserSummaryResponse>> getAllUserSummary(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute UserSpecificationParams specParams
    ) {
        PagedResponse<UserSummaryResponse> usr = userService.getAllUserSummary(reqParams, specParams);
        return new ResponseEntity<>(usr, HttpStatus.OK);
    }

    @Operation(summary = "Get user profile by username", description = "Fetches the public profile of a user by their username")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile returned successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping(path = "/api/v1/users/{username}")
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @Parameter(description = "The username of the user") @PathVariable String username) {
        UserProfileResponse upr = userService.getUserProfile(username);
        return new ResponseEntity<>(upr, HttpStatus.OK);
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "409", description = "Username or email already exists", content = @Content)
    })
    @PostMapping(path = "/api/v1/users")
    public ResponseEntity<UserProfileResponse> create(@Valid @RequestBody CreateUserRequest dto) {
        UserProfileResponse upr = userService.create(dto);
        return new ResponseEntity<>(upr, HttpStatus.CREATED);
    }

    @Operation(summary = "Update my profile", description = "Partially updates the authenticated user's profile information",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PatchMapping(path = "/api/v1/users/me/profile")
    public ResponseEntity<UserProfileResponse> partialUpdate(
            @Valid @RequestBody UpdateUserRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        UserProfileResponse upr = userService.partialUpdate(dto, principal.getUsername());
        return new ResponseEntity<>(upr, HttpStatus.OK);
    }

    @Operation(summary = "Change my password", description = "Changes the authenticated user's password",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or incorrect current password", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PatchMapping(path = "/api/v1/users/me/password")
    public ResponseEntity<GenericResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        GenericResponse gr = userService.changePassword(dto, principal.getUsername());
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }

    @Operation(summary = "Get my profile", description = "Returns the profile of the currently authenticated user",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping(path = "/api/v1/users/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal UserDetails principal) {
        String currentUserUsername = (principal != null)
                ? principal.getUsername() : null;
        UserProfileResponse myProfile = userService.getUserProfile(currentUserUsername);
        return new ResponseEntity<>(myProfile, HttpStatus.OK);
    }

    @Operation(summary = "Delete my profile", description = "Permanently deletes or schedules deletion of the currently authenticated user's profile",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @DeleteMapping(path = "/api/v1/users/me")
    public ResponseEntity<GenericResponse> deleteProfile(
            @AuthenticationPrincipal CustomUserDetails principal) {
        GenericResponse gr = userService.deleteProfile(principal.getUsername());
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }
}
