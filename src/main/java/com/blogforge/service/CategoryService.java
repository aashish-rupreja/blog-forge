package com.blogforge.service;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.category.CategoryResponse;
import com.blogforge.dto.category.CreateCategoryRequest;
import com.blogforge.dto.category.DeleteCategoryRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.category.CategorySpecificationParams;

public interface CategoryService {

    PagedResponse<CategoryResponse> getAll(PaginationRequestParams reqParams, CategorySpecificationParams specParams);

    CategoryResponse getByName(String categoryName);

    CategoryResponse create(CreateCategoryRequest dto);

    GenericResponse delete(DeleteCategoryRequest dto);
}
