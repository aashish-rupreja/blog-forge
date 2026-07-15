package com.blogforge.service;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.blog.BlogDetailsResponse;
import com.blogforge.dto.blog.BlogSummaryResponse;
import com.blogforge.dto.blog.CreateBlogRequest;
import com.blogforge.dto.blog.UpdateBlogRequest;
import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.dto.reaction.AddReactionRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.specification.blog.BlogSpecificationParams;

import java.util.List;
import java.util.UUID;

public interface BlogService {

    PagedResponse<BlogSummaryResponse> getAllSummary(PaginationRequestParams reqParams, BlogSpecificationParams specParams);

    BlogDetailsResponse getBlogDetails(String slug);

    PagedResponse<CommentResponse> getBlogComments(String slug, PaginationRequestParams requestParams);

    BlogDetailsResponse partialUpdate(String slug, UpdateBlogRequest updateBlogRequest);

    GenericResponse delete(String slug);

    PagedResponse<BlogSummaryResponse> getMyBlogs(PaginationRequestParams reqParams);

    BlogDetailsResponse create(CreateBlogRequest dto);

    GenericResponse hardDelete(List<UUID> uuids);

    GenericResponse like(String slug, AddReactionRequest dto, CustomUserDetails principal);
}
