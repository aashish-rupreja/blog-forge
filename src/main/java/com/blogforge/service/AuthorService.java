package com.blogforge.service;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.user.UserProfileResponse;
import com.blogforge.dto.user.UserSummaryResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.user.UserSpecificationParams;

public interface AuthorService {

    PagedResponse<UserSummaryResponse> getAllAuthorSummary(PaginationRequestParams reqParams, UserSpecificationParams specParams);

    UserProfileResponse getAuthorProfile(String username);
}
