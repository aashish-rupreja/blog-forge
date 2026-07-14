package com.blogforge.service;

import com.blogforge.dto.authorapplication.AuthorApplicationResponse;
import com.blogforge.dto.authorapplication.CreateAuthorApplicationRequest;
import com.blogforge.dto.authorapplication.MyAuthorApplicationsRequest;
import com.blogforge.dto.authorapplication.UpdateAuthorApplicationRequest;
import com.blogforge.entity.AuthorApplication;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.authorapplication.AuthorApplicationSpecificationParams;

import java.util.UUID;

public interface AuthorApplicationService {

    PagedResponse<AuthorApplicationResponse> getAll(PaginationRequestParams reqParams, AuthorApplicationSpecificationParams specParams);

    PagedResponse<AuthorApplicationResponse> getMyAuthorApplications(PaginationRequestParams reqParams, MyAuthorApplicationsRequest specParams);

    AuthorApplicationResponse create(CreateAuthorApplicationRequest dto);

    AuthorApplicationResponse getSingleApplication(UUID id);

    AuthorApplicationResponse approveApplication(UUID id, UpdateAuthorApplicationRequest dto);

    AuthorApplicationResponse rejectApplication(UUID id, UpdateAuthorApplicationRequest dto);
}
