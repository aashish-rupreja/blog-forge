package com.blogforge.service;

import com.blogforge.dto.category.CategoryResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.category.CategorySpecificationParams;

public interface CategoryService {

    PagedResponse<CategoryResponse> getAll(PaginationRequestParams reqParams, CategorySpecificationParams specParams);
}
