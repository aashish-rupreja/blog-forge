package com.blogforge.dto.role;

import com.blogforge.dto.BaseResponse;
import com.blogforge.entity.RoleType;

import java.util.Set;

public record RoleResponse(
        BaseResponse commonFields,
        String name,
        RoleType roleType,
        Set<String> holders,
        long holderCount
) {
}
