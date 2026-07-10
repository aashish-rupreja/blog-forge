package com.blogforge.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTagRequest(
        @NotBlank(message = "{tag.name.blank}")
        @Size(min = 3, max = 10, message = "{tag.name.size}")
        String name
) {
}