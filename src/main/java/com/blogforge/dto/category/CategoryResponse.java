package com.blogforge.dto.category;

import com.blogforge.dto.BaseResponse;

public record CategoryResponse(
        BaseResponse commonFields,
        String name,
        long blogCount
) {
}
