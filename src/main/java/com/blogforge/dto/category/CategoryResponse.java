package com.blogforge.dto.category;

import com.blogforge.dto.BaseResponse;

public record CategoryResponse(
        BaseResponse baseResponse,
        String name,
        long blogCount
) {
}
