package com.blogforge.service.impl;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.dto.comment.CreateCommentRequest;
import com.blogforge.dto.comment.UpdateCommentRequest;
import com.blogforge.entity.Blog;
import com.blogforge.entity.Comment;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.CommentMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.BlogRepository;
import com.blogforge.repository.CommentRepository;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.service.CommentService;
import com.blogforge.specification.comment.CommentSpecification;
import com.blogforge.specification.comment.CommentSpecificationParams;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {

    private final static Logger LOG = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final CommentMapper commentMapper;
    private final MessageResolver messageResolver;

    public CommentServiceImpl(CommentRepository commentRepository, BlogRepository blogRepository, CommentMapper commentMapper, MessageResolver messageResolver) {
        this.commentRepository = commentRepository;
        this.blogRepository = blogRepository;
        this.commentMapper = commentMapper;
        this.messageResolver = messageResolver;
    }

    @Override
    public PagedResponse<CommentResponse> getAll(PaginationRequestParams reqParams, CommentSpecificationParams specParams) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<Comment> spec = CommentSpecification.handleSpecs(specParams);

        Page<Comment> comments = commentRepository.findAll(spec, jpaPageable);
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

    @Override
    @Transactional
    public CommentResponse addComment(String slug, CreateCommentRequest dto, User commentor) {
        Comment c = commentMapper.fromCreateRequestToEntity(dto);
        c.setOwner(commentor);

        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> {
                    String blogNotFoundMsg = messageResolver.getMessage(
                            "entity.not-found",
                            "Blog", slug);
                    LOG.warn(blogNotFoundMsg);
                    return new EntityNotFoundException(blogNotFoundMsg);
                });
        c.setBlog(b);
        Comment saved = commentRepository.save(c);

        return commentMapper.fromEntityToResponse(saved);
    }

    @Override
    public CommentResponse partialUpdate(UUID commentId, UpdateCommentRequest dto, String commentOwnerUsername) {
        Comment c = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    String commentNotFoundMsg = messageResolver.getMessage(
                            "entity.not-found",
                            "Comment", commentId
                    );
                    LOG.warn(commentNotFoundMsg);
                    return new EntityNotFoundException(commentNotFoundMsg);
                });
        if(!c.getOwner().getUsername().equals(commentOwnerUsername)) {
            String accessDeniedMsg = messageResolver.getMessage("comment.edit.access-denied");
            LOG.warn(accessDeniedMsg);
            throw new AccessDeniedException(accessDeniedMsg);
        }
        c.setContent(dto.content());
        Comment saved = commentRepository.save(c);
        return commentMapper.fromEntityToResponse(saved);

    }

    @Override
    @Transactional
    public GenericResponse delete(UUID id, CustomUserDetails principal) {
        Comment c = commentRepository.findById(id)
                .orElseThrow(() -> {
                    String commentNotFoundMsg = messageResolver.getMessage(
                            "entity.not-found",
                            "Comment", id
                    );
                    LOG.warn(commentNotFoundMsg);
                    return new EntityNotFoundException(commentNotFoundMsg);
                });


        // allow delete comment under these conditions
        // delete requested by comment owner
        boolean isCommentOwner = c.getOwner().getUsername().equals(principal.getUsername());

        // delete requested by blog author
        boolean isBlogAuthor = c.getBlog().getAuthor().getUsername().equals(principal.getUsername());

        // delete requested by admin
        boolean isAdmin = principal.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if(!(isCommentOwner || isBlogAuthor || isAdmin)) {
            String deleteAccessDenied = messageResolver.getMessage("comment.delete.access-denied");
            LOG.warn(deleteAccessDenied);
            throw new AccessDeniedException(deleteAccessDenied);
        }
        commentRepository.delete(c);
        LOG.info("Comment identified by {} is deleted", c.getUuid());
        return new GenericResponse(messageResolver.getMessage("comment.delete.msg"));
    }
}
