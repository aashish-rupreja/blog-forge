package com.blogforge.controller;

import com.blogforge.dto.authorapplication.AuthorApplicationResponse;
import com.blogforge.entity.AuthorApplication;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.AuthorApplicationService;
import com.blogforge.specification.authorapplication.AuthorApplicationSpecificationParams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

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
}
