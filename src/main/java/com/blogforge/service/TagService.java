package com.blogforge.service;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.tag.CreateTagRequest;
import com.blogforge.dto.tag.DeleteTagRequest;
import com.blogforge.dto.tag.TagResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;

public interface TagService {

    PagedResponse<TagResponse> getAll(PaginationRequestParams reqParams, String tagName);

    TagResponse getByName(String tagName);

    TagResponse create(CreateTagRequest tagRequest);

    GenericResponse delete(DeleteTagRequest tagRequest);
}
