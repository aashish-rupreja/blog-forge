package com.blogforge.dto.tag;

import com.blogforge.dto.BaseResponse;

public record TagResponse(
        BaseResponse commonFields,
        String name,
        long blogCount
) {
}
