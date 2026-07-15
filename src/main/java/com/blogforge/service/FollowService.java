package com.blogforge.service;

import com.blogforge.dto.GenericResponse;

public interface FollowService {

    GenericResponse create(String username);

    GenericResponse delete(String username, String authenticatedUsername);
}
