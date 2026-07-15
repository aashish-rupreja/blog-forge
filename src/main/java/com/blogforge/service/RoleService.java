package com.blogforge.service;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.role.CreateRoleRequest;
import com.blogforge.dto.role.DeleteRoleRequest;
import com.blogforge.dto.role.RoleResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.role.RoleSpecificationParams;

public interface RoleService {

    PagedResponse<RoleResponse> getAll(PaginationRequestParams paginationRequestParams,
                                       RoleSpecificationParams roleSpecificationParams);

    RoleResponse getByName(String name);

    RoleResponse create(CreateRoleRequest createRoleRequest);

    GenericResponse deleteOne(String name);

    GenericResponse deleteAllIn(DeleteRoleRequest deleteRoleRequest);
}
