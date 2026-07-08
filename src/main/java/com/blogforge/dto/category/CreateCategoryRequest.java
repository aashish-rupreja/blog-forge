package com.blogforge.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(

        @NotBlank(message = "{category.name.blank}")
        @Size(min = 3, max = 15, message = "{category.name.size}")
        @Pattern(
                regexp = "^[A-Za-z]+$",
                message = "{category.name.pattern}"
        )
        String name
) {
}
