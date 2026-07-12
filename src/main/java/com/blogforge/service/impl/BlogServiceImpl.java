package com.blogforge.service.impl;

import com.blogforge.dto.blog.BlogDetailsResponse;
import com.blogforge.dto.blog.BlogSummaryResponse;
import com.blogforge.entity.Blog;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.BlogMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.BlogRepository;
import com.blogforge.service.BlogService;
import com.blogforge.specification.blog.BlogSpecification;
import com.blogforge.specification.blog.BlogSpecificationParams;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class BlogServiceImpl implements BlogService {

    private final Logger LOG = LoggerFactory.getLogger(BlogServiceImpl.class);

    private final BlogRepository blogRepository;
    private final BlogMapper blogMapper;
    private final MessageResolver messageResolver;

    public BlogServiceImpl(BlogRepository blogRepository, BlogMapper blogMapper, MessageResolver messageResolver) {
        this.blogRepository = blogRepository;
        this.blogMapper = blogMapper;
        this.messageResolver = messageResolver;
    }

    public PagedResponse<BlogSummaryResponse> getAllSummary(PaginationRequestParams requestParams, BlogSpecificationParams specParams) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(requestParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<Blog> spec = BlogSpecification.handleSpecs(specParams);

        Page<Blog> blogSummaries = blogRepository.findAll(spec, jpaPageable);
        return new PagedResponse<>(
                blogSummaries.stream().map(blogMapper::fromEntityToSummaryResponse).toList(),
                blogSummaries.getNumber()+1,
                blogSummaries.getSize(),
                blogSummaries.getTotalPages(),
                blogSummaries.getTotalElements(),
                blogSummaries.isEmpty(),
                blogSummaries.hasNext()
        );
    }

    @Override
    public BlogDetailsResponse getBlogDetails(String slug) {
        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Blog", slug
                )));

        return blogMapper.fromEntityToDetailsResponse(b);
    }


}
