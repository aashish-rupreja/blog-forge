package com.blogforge.service;

import com.blogforge.dto.tag.TagResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;

public interface TagService {

    PagedResponse<TagResponse> getAll(PaginationRequestParams reqParams, String tagName);

    TagResponse getByName(String tagName);
}
