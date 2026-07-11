package com.blogforge.mapper;

import com.blogforge.dto.BaseResponse;
import com.blogforge.dto.category.*;
import com.blogforge.entity.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category fromCreateRequestToEntity(CreateCategoryRequest dto) {
        Category c = new Category();
        c.setName(dto.name());
        return c;
    }

    public CategoryResponse fromEntityToResponse(Category c) {
        long blogCount = (c.getBlogs() != null) ? c.getBlogs().size() : 0;
        return new CategoryResponse(
                new BaseResponse(
                        c.getUuid(),
                        c.getCreatedAt(),
                        c.getUpdatedAt()),
                c.getName(),
                blogCount
        );
    }
}
