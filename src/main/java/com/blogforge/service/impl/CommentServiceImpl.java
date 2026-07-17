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
import com.blogforge.repository.UserRepository;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentServiceImpl implements CommentService {

    private final static Logger LOG = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;
    private final MessageResolver messageResolver;

    public CommentServiceImpl(CommentRepository commentRepository, BlogRepository blogRepository, UserRepository userRepository, CommentMapper commentMapper, MessageResolver messageResolver) {
        this.commentRepository = commentRepository;
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.commentMapper = commentMapper;
        this.messageResolver = messageResolver;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PagedResponse<CommentResponse> getAll(PaginationRequestParams reqParams, CommentSpecificationParams specParams) {
        LOG.trace("Entering getAll with reqParams: {}, specParams: {}", reqParams, specParams);
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<Comment> spec = CommentSpecification.handleSpecs(specParams);

        LOG.trace("Fetching comments from repository with spec: {}", specParams);
        Page<Comment> comments = commentRepository.findAll(spec, jpaPageable);
        LOG.trace("Fetched {} comments", comments.getNumberOfElements());

        LOG.trace("Mapping Comment entities to response DTOs");
        PagedResponse<CommentResponse> response = new PagedResponse<>(
                comments.stream().map(commentMapper::fromEntityToResponse).toList(),
                comments.getNumber()+1,
                comments.getSize(),
                comments.getTotalPages(),
                comments.getTotalElements(),
                comments.isEmpty(),
                comments.hasNext()
        );
        LOG.trace("Exiting getAll with response count: {}", response.getContent().size());
        return response;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public CommentResponse addComment(String slug, CreateCommentRequest dto, String commentorUsername) {
        LOG.trace("Entering addComment with slug: {}, dto: {}, commentorUsername: {}", slug, dto, commentorUsername);
        
        LOG.trace("Finding user entity by username: {}", commentorUsername);
        User user = userRepository.findByUsernameIgnoreCase(commentorUsername)
                .orElseThrow(() -> {
                    String userNotFoundMsg = messageResolver.getMessage(
                            "entity.not-found",
                            "User", commentorUsername);
                    LOG.warn(userNotFoundMsg);
                    return new EntityNotFoundException(userNotFoundMsg);
                });

        LOG.trace("Mapping create request DTO to Comment entity");
        Comment c = commentMapper.fromCreateRequestToEntity(dto);
        c.setOwner(user);

        LOG.trace("Finding blog entity by slug: {}", slug);
        Blog b = blogRepository.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> {
                    String blogNotFoundMsg = messageResolver.getMessage(
                            "entity.not-found",
                            "Blog", slug);
                    LOG.warn(blogNotFoundMsg);
                    return new EntityNotFoundException(blogNotFoundMsg);
                });
        c.setBlog(b);
        
        LOG.trace("Saving new comment to repository");
        Comment saved = commentRepository.save(c);

        LOG.trace("Mapping saved Comment entity to response DTO");
        CommentResponse response = commentMapper.fromEntityToResponse(saved);
        LOG.trace("Exiting addComment with response: {}", response);
        return response;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public CommentResponse partialUpdate(UUID commentId, UpdateCommentRequest dto, String commentOwnerUsername) {
        LOG.trace("Entering partialUpdate with commentId: {}, dto: {}, commentOwnerUsername: {}", commentId, dto, commentOwnerUsername);
        
        LOG.trace("Finding comment by id: {}", commentId);
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
        
        LOG.trace("Saving updated comment to repository");
        Comment saved = commentRepository.save(c);
        
        LOG.trace("Mapping updated Comment entity to response DTO");
        CommentResponse response = commentMapper.fromEntityToResponse(saved);
        LOG.trace("Exiting partialUpdate with response: {}", response);
        return response;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_AUTHOR', 'ROLE_ADMIN')")
    public GenericResponse delete(UUID id, CustomUserDetails principal) {
        LOG.trace("Entering delete with id: {}, principal: {}", id, principal != null ? principal.getUsername() : null);
        
        LOG.trace("Finding comment by id: {}", id);
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
        
        LOG.trace("Deleting comment from repository: {}", c.getUuid());
        commentRepository.delete(c);
        LOG.info("Comment identified by {} is deleted", c.getUuid());
        
        String deletedMessage = messageResolver.getMessage("comment.deleted");
        LOG.trace("Exiting delete with message: {}", deletedMessage);
        return new GenericResponse(deletedMessage);
    }
}
