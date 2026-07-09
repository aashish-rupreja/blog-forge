package com.blogforge.controller;

import com.blogforge.dto.category.CategoryResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.CategoryService;
import com.blogforge.specification.category.CategorySpecificationParams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping(path = "/api/v1/categories")
    public ResponseEntity<PagedResponse<CategoryResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute CategorySpecificationParams specParams
            ) {
        PagedResponse<CategoryResponse> responses = categoryService.getAll(reqParams, specParams);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
