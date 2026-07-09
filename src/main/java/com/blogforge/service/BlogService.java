package com.blogforge.service;

import com.blogforge.dto.blog.BlogSummaryResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.blog.BlogSpecificationParams;

public interface BlogService {

    PagedResponse<BlogSummaryResponse> getAllSummary(PaginationRequestParams reqParams, BlogSpecificationParams specParams);
}
