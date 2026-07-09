package com.blogforge.pagination;

import org.springframework.data.domain.Sort;

public record PaginationRequestParams (
        Integer pageNo,
        Integer pageSize,
        Sort.Direction sortDirection,
        String sortBy
) {
}
