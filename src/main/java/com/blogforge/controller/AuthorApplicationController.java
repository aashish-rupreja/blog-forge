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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class AuthorApplicationController {

    private final AuthorApplicationService authorApplicationService;

    public AuthorApplicationController(AuthorApplicationService authorApplicationService) {
        this.authorApplicationService = authorApplicationService;
    }

    @GetMapping(path = "/api/v1/author-applications")
    public ResponseEntity<PagedResponse<AuthorApplicationResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute AuthorApplicationSpecificationParams specParams
            ) {
        PagedResponse<AuthorApplicationResponse> responses = authorApplicationService.getAll(reqParams, specParams);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping(path = "/api/v1/author-applications/{uuid}")
    public ResponseEntity<AuthorApplicationResponse> getSingleApplication(@PathVariable UUID uuid) {
        AuthorApplicationResponse aar = authorApplicationService.getSingleApplication(uuid);
        return new ResponseEntity<>(aar, HttpStatus.OK);
    }

    @GetMapping(path = "/api/v1/me/author-applications")
    public ResponseEntity<PagedResponse<AuthorApplicationResponse>> getMyAuthorApplications(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute MyAuthorApplicationsRequest specParams,
            @AuthenticationPrincipal CustomUserDetails principal
            ) {
        PagedResponse<AuthorApplicationResponse> responses = authorApplicationService.getMyAuthorApplications(reqParams, specParams, principal.getUsername());
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @PostMapping(path = "/api/v1/author-applications")
    public ResponseEntity<AuthorApplicationResponse> create(
            @Valid @RequestBody CreateAuthorApplicationRequest dto,
            @AuthenticationPrincipal CustomUserDetails principal) {
        AuthorApplicationResponse aar = authorApplicationService.create(dto, principal.getUsername());
        return new ResponseEntity<>(aar, HttpStatus.CREATED);
    }

    @PostMapping(path = "api/v1/author-applications/{id}/approve")
    public ResponseEntity<AuthorApplicationResponse> approveApplication(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAuthorApplicationRequest dto) {
        AuthorApplicationResponse aar = authorApplicationService.approveApplication(id, dto);
        return new ResponseEntity<>(aar, HttpStatus.OK);
    }

    @PostMapping(path = "/api/v1/author-applications/{id}/reject")
    public ResponseEntity<AuthorApplicationResponse> rejectApplication(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAuthorApplicationRequest dto
    ) {
        AuthorApplicationResponse aar = authorApplicationService.rejectApplication(id, dto);
        return new ResponseEntity<>(aar, HttpStatus.OK);
    }
}
