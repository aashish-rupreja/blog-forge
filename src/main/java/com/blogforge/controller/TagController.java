package com.blogforge.controller;

import com.blogforge.dto.tag.TagResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.TagService;
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
}
