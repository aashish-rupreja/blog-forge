package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.category.CategoryResponse;
import com.blogforge.dto.category.CreateCategoryRequest;
import com.blogforge.dto.category.DeleteCategoryRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.CategoryService;
import com.blogforge.specification.category.CategorySpecificationParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Categories", description = "Endpoints for managing blog categories")
@RestController
public class CategoryController {

    private static final Logger LOG = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Get all categories", description = "Returns a paginated list of categories. Supports filtering/searching.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of categories returned successfully")
    })
    @GetMapping(path = "/api/v1/categories")
    public ResponseEntity<PagedResponse<CategoryResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute CategorySpecificationParams specParams
            ) {
        LOG.trace("Entering getAll with reqParams: {}, specParams: {}", reqParams, specParams);
        PagedResponse<CategoryResponse> responses = categoryService.getAll(reqParams, specParams);
        LOG.trace("Exiting getAll with responses count: {}", responses.getContent() != null ? responses.getContent().size() : 0);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation(summary = "Get category by name", description = "Fetches a specific category by its unique name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category returned successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @GetMapping(path = "/api/v1/categories/{name}")
    public ResponseEntity<CategoryResponse> getByName(
            @Parameter(description = "The unique name of the category") @PathVariable String name) {
        LOG.trace("Entering getByName with name: {}", name);
        CategoryResponse cr = categoryService.getByName(name);
        LOG.trace("Exiting getByName with response: {}", cr);
        return new ResponseEntity<>(cr, HttpStatus.OK);
    }

    @Operation(summary = "Create a category", description = "Creates a new category.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "409", description = "Category already exists", content = @Content)
    })
    @PostMapping(path = "/api/v1/categories")
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest dto) {
        LOG.trace("Entering create with dto: {}", dto);
        CategoryResponse cr = categoryService.create(dto);
        LOG.trace("Exiting create with response: {}", cr);
        return new ResponseEntity<>(cr, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete categories", description = "Deletes specified categories.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "One or more categories not found", content = @Content)
    })
    @DeleteMapping(path = "/api/v1/categories")
    public ResponseEntity<GenericResponse> delete(@Valid @RequestBody DeleteCategoryRequest dto) {
        LOG.trace("Entering delete with dto: {}", dto);
        GenericResponse gr = categoryService.delete(dto);
        LOG.trace("Exiting delete with response: {}", gr);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }
}
