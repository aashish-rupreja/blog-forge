package com.blogforge.controller;

import com.blogforge.dto.authorapplication.AuthorApplicationResponse;
import com.blogforge.dto.authorapplication.CreateAuthorApplicationRequest;
import com.blogforge.dto.authorapplication.MyAuthorApplicationsRequest;
import com.blogforge.dto.authorapplication.UpdateAuthorApplicationRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.service.AuthorApplicationService;
import com.blogforge.specification.authorapplication.AuthorApplicationSpecificationParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Author Applications", description = "Endpoints for managing user applications to become authors")
@RestController
public class AuthorApplicationController {

    private final AuthorApplicationService authorApplicationService;

    public AuthorApplicationController(AuthorApplicationService authorApplicationService) {
        this.authorApplicationService = authorApplicationService;
    }

    @Operation(summary = "Get all author applications (Admin only)", description = "Returns a paginated list of all author applications. Requires Admin privileges.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of applications returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content)
    })
    @GetMapping(path = "/api/v1/author-applications")
    public ResponseEntity<PagedResponse<AuthorApplicationResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute AuthorApplicationSpecificationParams specParams
            ) {
        PagedResponse<AuthorApplicationResponse> responses = authorApplicationService.getAll(reqParams, specParams);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation(summary = "Get single author application", description = "Fetches a specific application by UUID. Requires Admin or owner access.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author application details returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - access denied", content = @Content),
            @ApiResponse(responseCode = "404", description = "Application not found", content = @Content)
    })
    @GetMapping(path = "/api/v1/author-applications/{uuid}")
    public ResponseEntity<AuthorApplicationResponse> getSingleApplication(
            @Parameter(description = "The UUID of the author application") @PathVariable UUID uuid) {
        AuthorApplicationResponse aar = authorApplicationService.getSingleApplication(uuid);
        return new ResponseEntity<>(aar, HttpStatus.OK);
    }

    @Operation(summary = "Get my author applications", description = "Returns a paginated list of author applications submitted by the authenticated user.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of user's applications returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping(path = "/api/v1/me/author-applications")
    public ResponseEntity<PagedResponse<AuthorApplicationResponse>> getMyAuthorApplications(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute MyAuthorApplicationsRequest specParams,
            @AuthenticationPrincipal CustomUserDetails principal
            ) {
        PagedResponse<AuthorApplicationResponse> responses = authorApplicationService.getMyAuthorApplications(reqParams, specParams, principal.getUsername());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation(summary = "Submit an author application", description = "Creates a new author application for the authenticated user.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Application submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or existing active application", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping(path = "/api/v1/author-applications")
    public ResponseEntity<AuthorApplicationResponse> create(
            @Valid @RequestBody CreateAuthorApplicationRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        AuthorApplicationResponse aar = authorApplicationService.create(dto, principal.getUsername());
        return new ResponseEntity<>(aar, HttpStatus.CREATED);
    }

    @Operation(summary = "Approve an author application (Admin only)", description = "Approves a pending author application. Requires Admin privileges.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application approved successfully"),
            @ApiResponse(responseCode = "400", description = "Application not in pending state", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Application not found", content = @Content)
    })
    @PostMapping(path = "api/v1/author-applications/{id}/approve")
    public ResponseEntity<AuthorApplicationResponse> approveApplication(
            @Parameter(description = "The UUID of the application to approve") @PathVariable UUID id,
            @Valid @RequestBody UpdateAuthorApplicationRequest dto) {
        AuthorApplicationResponse aar = authorApplicationService.approveApplication(id, dto);
        return new ResponseEntity<>(aar, HttpStatus.OK);
    }

    @Operation(summary = "Reject an author application (Admin only)", description = "Rejects a pending author application. Requires Admin privileges.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Application rejected successfully"),
            @ApiResponse(responseCode = "400", description = "Application not in pending state", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Application not found", content = @Content)
    })
    @PostMapping(path = "/api/v1/author-applications/{id}/reject")
    public ResponseEntity<AuthorApplicationResponse> rejectApplication(
            @Parameter(description = "The UUID of the application to reject") @PathVariable UUID id,
            @Valid @RequestBody UpdateAuthorApplicationRequest dto
    ) {
        AuthorApplicationResponse aar = authorApplicationService.rejectApplication(id, dto);
        return new ResponseEntity<>(aar, HttpStatus.OK);
    }
}
