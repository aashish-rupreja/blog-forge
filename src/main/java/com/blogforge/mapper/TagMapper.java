package com.blogforge.mapper;

import com.blogforge.dto.BaseResponse;
import com.blogforge.dto.tag.CreateTagRequest;
import com.blogforge.dto.tag.TagResponse;
import com.blogforge.entity.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper {

    public Tag fromCreateRequestToEntity(CreateTagRequest dto) {
        Tag t = new Tag();
        t.setName(dto.name());
        return t;
    }

    public TagResponse fromEntityToResponse(Tag t) {
        return new TagResponse(
                new BaseResponse(
                        t.getUuid(),
                        t.getCreatedAt(),
                        t.getUpdatedAt()
                ),
                t.getName(),
                t.getBlogs().size()
        );
    }
}
