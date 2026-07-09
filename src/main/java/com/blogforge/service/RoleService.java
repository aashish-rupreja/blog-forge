package com.blogforge.service;

import com.blogforge.dto.role.RoleResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.role.RoleSpecificationParams;

public interface RoleService {

    PagedResponse<RoleResponse> getAll(PaginationRequestParams reqParams, RoleSpecificationParams specParams);
}
