package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.category.DeleteCategoryRequest;
import com.blogforge.dto.tag.CreateTagRequest;
import com.blogforge.dto.tag.DeleteTagRequest;
import com.blogforge.dto.tag.TagResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.TagService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping("/api/v1/tags")
    public ResponseEntity<PagedResponse<TagResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @RequestParam(required = false) String name
            ) {
        PagedResponse<TagResponse> response = tagService.getAll(reqParams, name);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/api/v1/tags/{name}")
    public ResponseEntity<TagResponse> getByName(@PathVariable String name) {
        TagResponse tr = tagService.getByName(name);
        return new ResponseEntity<>(tr, HttpStatus.OK);
    }

    @PostMapping("/api/v1/tags")
    public ResponseEntity<TagResponse> create(@Valid @RequestBody CreateTagRequest tagRequest) {
        TagResponse tr = tagService.create(tagRequest);
        return new ResponseEntity<>(tr, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/api/v1/tags")
    public ResponseEntity<GenericResponse> delete(@Valid @RequestBody DeleteTagRequest dto) {
        GenericResponse gr = tagService.delete(dto);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }
}
