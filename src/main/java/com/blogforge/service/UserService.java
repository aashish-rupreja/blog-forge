package com.blogforge.service;

import com.blogforge.dto.user.CreateUserRequest;
import com.blogforge.dto.user.UserProfileResponse;
import com.blogforge.dto.user.UserSummaryResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.user.UserSpecificationParams;

import java.util.List;

public interface UserService {

    PagedResponse<UserSummaryResponse> getAllUserSummary(PaginationRequestParams reqParams, UserSpecificationParams specParams);

    UserProfileResponse getUserProfile(String username);

    UserProfileResponse create(CreateUserRequest dto);
}
