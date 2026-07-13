package com.blogforge.service;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.blog.BlogDetailsResponse;
import com.blogforge.dto.blog.BlogSummaryResponse;
import com.blogforge.dto.blog.UpdateBlogRequest;
import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.specification.blog.BlogSpecificationParams;

public interface BlogService {

    PagedResponse<BlogSummaryResponse> getAllSummary(PaginationRequestParams reqParams, BlogSpecificationParams specParams);

    BlogDetailsResponse getBlogDetails(String slug);

    PagedResponse<CommentResponse> getBlogComments(String slug, PaginationRequestParams requestParams);

    BlogDetailsResponse partialUpdate(String slug, UpdateBlogRequest updateBlogRequest);

    GenericResponse delete(String slug);

    PagedResponse<BlogSummaryResponse> getMyBlogs(PaginationRequestParams reqParams);
}
