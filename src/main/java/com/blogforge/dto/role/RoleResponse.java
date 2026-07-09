package com.blogforge.dto.role;

import com.blogforge.dto.BaseResponse;

import java.util.Set;

public record RoleResponse(
        BaseResponse commonFields,
        String name,
        Set<String> holders,
        long holderCount
) {
}
