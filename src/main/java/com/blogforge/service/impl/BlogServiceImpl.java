package com.blogforge.service.impl;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.blog.BlogDetailsResponse;
import com.blogforge.dto.blog.BlogSummaryResponse;
import com.blogforge.dto.blog.UpdateBlogRequest;
import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.entity.*;
import com.blogforge.exception.IllegalBlogTransitionException;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.BlogMapper;
import com.blogforge.mapper.CommentMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.BlogRepository;
import com.blogforge.repository.CategoryRepository;
import com.blogforge.repository.CommentRepository;
import com.blogforge.repository.TagRepository;
import com.blogforge.service.BlogService;
import com.blogforge.specification.blog.BlogSpecification;
import com.blogforge.specification.blog.BlogSpecificationParams;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements BlogService {

    private final Logger LOG = LoggerFactory.getLogger(BlogServiceImpl.class);

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final BlogMapper blogMapper;
    private final CommentMapper commentMapper;
    private final MessageResolver messageResolver;

    public BlogServiceImpl(
            BlogRepository blogRepository,
            CommentRepository commentRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            BlogMapper blogMapper,
            CommentMapper commentMapper,
            MessageResolver messageResolver) {
        this.blogRepository = blogRepository;
        this.commentRepository = commentRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.blogMapper = blogMapper;
        this.commentMapper = commentMapper;
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

    @Override
    public PagedResponse<CommentResponse> getBlogComments(String slug, PaginationRequestParams requestParams) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(requestParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Page<Comment> comments = commentRepository.findByBlog_Slug(slug, jpaPageable);

        return new PagedResponse<>(
                comments.stream().map(commentMapper::fromEntityToResponse).toList(),
                comments.getNumber()+1,
                comments.getSize(),
                comments.getTotalPages(),
                comments.getTotalElements(),
                comments.isEmpty(),
                comments.hasNext()
        );
    }

    public BlogDetailsResponse partialUpdate(String slug, UpdateBlogRequest updateBlogRequest) {
        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Blog", slug
                )));

        if(updateBlogRequest.title() != null) {
            b.setTitle(updateBlogRequest.title());
            b.setSlug(generateSlug(updateBlogRequest.title()));
        }
        if(updateBlogRequest.content() != null) {
            b.setContent(updateBlogRequest.content());
        }
        if(updateBlogRequest.enableComments() != null) {
            b.setEnableComments(updateBlogRequest.enableComments());
        }
        if(updateBlogRequest.blogStatus() != null) {
            if(!b.getStatus().canTransitionTo(updateBlogRequest.blogStatus().toString())) {
                throw new IllegalBlogTransitionException(messageResolver.getMessage("blog.transition.illegal", b.getStatus().toString(), updateBlogRequest.blogStatus().toString()));
            }
            b.setStatus(updateBlogRequest.blogStatus());
        }
        if(updateBlogRequest.categories() != null) {
            Set<Category> categories = new HashSet<>(categoryRepository.findByNameIn(updateBlogRequest.categories()));
            b.setCategories(categories);
        }
        if(updateBlogRequest.tags() != null) {
            Set<String> currentTags = b.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
            Set<Tag> newTagsToAdd = new HashSet<>(b.getTags());

            for(String tagToUpdate : updateBlogRequest.tags()) {

                // check if blog already has incoming tag
                if(!currentTags.contains(tagToUpdate)) {

                    // check if incoming tag exists
                    Optional<Tag> t = tagRepository.findByNameIgnoreCase(tagToUpdate);

                    // create new tag
                    if(t.isEmpty()) {
                        Tag newTag = new Tag();
                        newTag.setName(tagToUpdate);
                        Tag saved = tagRepository.save(newTag);
                        newTagsToAdd.add(saved);
                    } else {
                        newTagsToAdd.add(t.get());
                    }
                }

                // at the end of this loop newTagsToAdd contains the new tags to be added to the blog
            }

            b.setTags(newTagsToAdd);
        }

        Blog saved = blogRepository.save(b);
        return blogMapper.fromEntityToDetailsResponse(saved);
    }

    @Override
    public GenericResponse delete(String slug) {
        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Blog", slug
                )));

        if(!b.getStatus().canTransitionTo(BlogStatus.DELETED.toString())) {
            throw new IllegalBlogTransitionException(messageResolver.getMessage("blog.transition.illegal", b.getStatus().toString(), BlogStatus.DELETED.toString()));
        }
        b.setStatus(BlogStatus.DELETED);
        blogRepository.save(b);
        String deleteMessage = messageResolver.getMessage("blog.blogStatus.deleted", b.getTitle());
        return new GenericResponse(deleteMessage);
    }

    @Override
    public PagedResponse<BlogSummaryResponse> getMyBlogs(PaginationRequestParams reqParams) {
        String currentAuthenticatedUser = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Page<Blog> myBlogs = blogRepository.findAllByAuthor_UsernameIgnoreCase(currentAuthenticatedUser, jpaPageable);

        return new PagedResponse<>(
                myBlogs.stream().map(blogMapper::fromEntityToSummaryResponse).toList(),
                myBlogs.getNumber()+1,
                myBlogs.getSize(),
                myBlogs.getTotalPages(),
                myBlogs.getTotalElements(),
                myBlogs.isEmpty(),
                myBlogs.hasNext()
        );
    }

    private String generateSlug(String title) {
        return title
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
