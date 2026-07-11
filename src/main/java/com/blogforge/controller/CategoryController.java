package com.blogforge.controller;

import com.blogforge.dto.category.CategoryResponse;
import com.blogforge.dto.category.CreateCategoryRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.CategoryService;
import com.blogforge.specification.category.CategorySpecificationParams;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(path = "/api/v1/categories/{name}")
    public ResponseEntity<CategoryResponse> getByName(@PathVariable  String name) {
        CategoryResponse cr = categoryService.getByName(name);
        return new ResponseEntity<>(cr, HttpStatus.OK);
    }

    @PostMapping(path = "/api/v1/categories")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest dto) {
        CategoryResponse cr = categoryService.create(dto);
        return new ResponseEntity<>(cr, HttpStatus.CREATED);
    }
}
