package com.blogforge.service;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.user.*;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.user.UserSpecificationParams;

import java.util.List;

public interface UserService {

    PagedResponse<UserSummaryResponse> getAllUserSummary(PaginationRequestParams reqParams, UserSpecificationParams specParams);

    UserProfileResponse getUserProfile(String username);

    UserProfileResponse create(CreateUserRequest dto);

    UserProfileResponse partialUpdate(String username, UpdateUserRequest dto);

    GenericResponse changePassword(String username, ChangePasswordRequest dto);
}
