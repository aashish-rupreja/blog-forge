package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.category.DeleteCategoryRequest;
import com.blogforge.dto.tag.CreateTagRequest;
import com.blogforge.dto.tag.DeleteTagRequest;
import com.blogforge.dto.tag.TagResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.TagService;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "Tags", description = "Endpoints for managing blog tags")
@RestController
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Operation(summary = "Get all tags", description = "Returns a paginated list of tags. Supports optional filtering by name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of tags returned successfully")
    })
    @GetMapping("/api/v1/tags")
    public ResponseEntity<PagedResponse<TagResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @Parameter(description = "Filter tags by name prefix or containing term") @RequestParam(required = false) String name
            ) {
        PagedResponse<TagResponse> response = tagService.getAll(reqParams, name);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get tag by name", description = "Fetches a specific tag by its name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag returned successfully"),
            @ApiResponse(responseCode = "404", description = "Tag not found", content = @Content)
    })
    @GetMapping("/api/v1/tags/{name}")
    public ResponseEntity<TagResponse> getByName(
            @Parameter(description = "The unique name of the tag") @PathVariable String name) {
        TagResponse tr = tagService.getByName(name);
        return new ResponseEntity<>(tr, HttpStatus.OK);
    }

    @Operation(summary = "Create a tag", description = "Creates a new tag.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "409", description = "Tag already exists", content = @Content)
    })
    @PostMapping("/api/v1/tags")
    public ResponseEntity<TagResponse> create(@Valid @RequestBody CreateTagRequest tagRequest) {
        TagResponse tr = tagService.create(tagRequest);
        return new ResponseEntity<>(tr, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete tags", description = "Deletes specified tags.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tags deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "One or more tags not found", content = @Content)
    })
    @DeleteMapping(path = "/api/v1/tags")
    public ResponseEntity<GenericResponse> delete(@Valid @RequestBody DeleteTagRequest dto) {
        GenericResponse gr = tagService.delete(dto);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }
}
