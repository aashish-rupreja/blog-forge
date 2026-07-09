package com.blogforge.controller;

import com.blogforge.dto.blog.BlogSummaryResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.BlogService;
import com.blogforge.specification.blog.BlogSpecificationParams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BlogController {

    private final BlogService blogService;

    public BlogController(BlogService blogService) {
        this.blogService = blogService;
    }

    @GetMapping(path = "/api/v1/blogs")
    public ResponseEntity<PagedResponse<BlogSummaryResponse>> getAllSummary(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute BlogSpecificationParams specParams
            ) {

        PagedResponse<BlogSummaryResponse> blogs = blogService.getAllSummary(reqParams, specParams);
        return new ResponseEntity<>(blogs, HttpStatus.OK);

    }
}
