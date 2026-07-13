package com.blogforge.service;

import com.blogforge.dto.authorapplication.AuthorApplicationResponse;
import com.blogforge.dto.authorapplication.CreateAuthorApplicationRequest;
import com.blogforge.dto.authorapplication.MyAuthorApplicationsRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.authorapplication.AuthorApplicationSpecificationParams;

public interface AuthorApplicationService {

    PagedResponse<AuthorApplicationResponse> getAll(PaginationRequestParams reqParams, AuthorApplicationSpecificationParams specParams);

    PagedResponse<AuthorApplicationResponse> getMyAuthorApplications(PaginationRequestParams reqParams, MyAuthorApplicationsRequest specParams);

    AuthorApplicationResponse create(CreateAuthorApplicationRequest dto);
}
