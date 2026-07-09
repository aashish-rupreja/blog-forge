package com.blogforge.service;

import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.comment.CommentSpecificationParams;

public interface CommentService {

    PagedResponse<CommentResponse> getAll(PaginationRequestParams reqParams, CommentSpecificationParams specParams);
}
