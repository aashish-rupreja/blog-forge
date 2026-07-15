package com.blogforge.service.impl;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.comment.CommentResponse;
import com.blogforge.dto.comment.CreateCommentRequest;
import com.blogforge.dto.comment.UpdateCommentRequest;
import com.blogforge.entity.Blog;
import com.blogforge.entity.Comment;
import com.blogforge.entity.Role;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.CommentMapper;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.BlogRepository;
import com.blogforge.repository.CommentRepository;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.specification.comment.CommentSpecificationParams;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock private CommentRepository commentRepository;
    @Mock private BlogRepository blogRepository;
    @Mock private CommentMapper commentMapper;
    @Mock private MessageResolver messageResolver;

    @InjectMocks
    private CommentServiceImpl service;

    private User commentOwner;
    private User blogAuthor;
    private Blog blog;
    private Comment comment;
    private CommentResponse commentResponse;
    private PaginationRequestParams defaultPaging;

    @BeforeEach
    void setUp() {
        commentOwner = new User();
        commentOwner.setUsername("commenter");
        commentOwner.setRoles(new HashSet<>());

        blogAuthor = new User();
        blogAuthor.setUsername("author1");
        blogAuthor.setRoles(new HashSet<>());

        blog = new Blog();
        blog.setSlug("test-blog");
        blog.setAuthor(blogAuthor);

        comment = new Comment();
        comment.setOwner(commentOwner);
        comment.setBlog(blog);
        comment.setContent("Hello world");

        commentResponse = mock(CommentResponse.class);
        defaultPaging = new PaginationRequestParams(1, 10, null, null);
    }

    // ── addComment ─────────────────────────────────────────────────────────────

    @Test
    void addComment_ShouldSaveComment_WhenBlogExists() {
        CreateCommentRequest dto = mock(CreateCommentRequest.class);

        when(commentMapper.fromCreateRequestToEntity(dto)).thenReturn(comment);
        when(blogRepository.findBySlugIgnoreCase("test-blog")).thenReturn(Optional.of(blog));
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.fromEntityToResponse(comment)).thenReturn(commentResponse);

        CommentResponse result = service.addComment("test-blog", dto, commentOwner);

        assertNotNull(result);
        assertEquals(commentOwner, comment.getOwner());
        assertEquals(blog, comment.getBlog());
        verify(commentRepository).save(comment);
    }

    @Test
    void addComment_ShouldThrowEntityNotFoundException_WhenBlogDoesNotExist() {
        CreateCommentRequest dto = mock(CreateCommentRequest.class);
        when(commentMapper.fromCreateRequestToEntity(dto)).thenReturn(comment);
        when(blogRepository.findBySlugIgnoreCase("missing"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Blog"), eq("missing")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class,
                () -> service.addComment("missing", dto, commentOwner));
        verify(commentRepository, never()).save(any());
    }

    // ── partialUpdate ──────────────────────────────────────────────────────────

    @Test
    void partialUpdate_ShouldUpdateComment_WhenUserIsCommentOwner() {
        UUID id = UUID.randomUUID();
        UpdateCommentRequest dto = new UpdateCommentRequest("Updated content");

        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.fromEntityToResponse(comment)).thenReturn(commentResponse);

        CommentResponse result = service.partialUpdate(id, dto, "commenter");

        assertNotNull(result);
        assertEquals("Updated content", comment.getContent());
    }

    @Test
    void partialUpdate_ShouldThrowAccessDeniedException_WhenUserIsNotCommentOwner() {
        UUID id = UUID.randomUUID();
        UpdateCommentRequest dto = new UpdateCommentRequest("Hacked");

        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        when(messageResolver.getMessage("comment.edit.access-denied")).thenReturn("access denied");

        assertThrows(AccessDeniedException.class,
                () -> service.partialUpdate(id, dto, "attacker"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void partialUpdate_ShouldThrowEntityNotFoundException_WhenCommentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findById(id)).thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Comment"), eq(id)))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class,
                () -> service.partialUpdate(id, new UpdateCommentRequest("x"), "commenter"));
    }

    // ── delete ─────────────────────────────────────────────────────────────────

    @Test
    void delete_ShouldDeleteComment_WhenUserIsCommentOwner() {
        UUID id = UUID.randomUUID();
        CustomUserDetails principal = new CustomUserDetails(commentOwner);

        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        when(messageResolver.getMessage("comment.delete.msg")).thenReturn("deleted");

        GenericResponse result = service.delete(id, principal);

        assertNotNull(result);
        verify(commentRepository).delete((Comment) comment);
    }

    @Test
    void delete_ShouldDeleteComment_WhenUserIsBlogAuthor() {
        UUID id = UUID.randomUUID();
        CustomUserDetails principal = new CustomUserDetails(blogAuthor);

        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        when(messageResolver.getMessage("comment.delete.msg")).thenReturn("deleted");

        GenericResponse result = service.delete(id, principal);

        assertNotNull(result);
        verify(commentRepository).delete((Comment) comment);
    }

    @Test
    void delete_ShouldDeleteComment_WhenUserIsAdmin() {
        UUID id = UUID.randomUUID();

        User admin = new User();
        admin.setUsername("admin");
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        admin.setRoles(Set.of(adminRole));

        CustomUserDetails principal = new CustomUserDetails(admin);

        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        when(messageResolver.getMessage("comment.delete.msg")).thenReturn("deleted");

        GenericResponse result = service.delete(id, principal);

        assertNotNull(result);
        verify(commentRepository).delete((Comment) comment);
    }

    @Test
    void delete_ShouldThrowAccessDeniedException_WhenUserHasNoDeletePermission() {
        UUID id = UUID.randomUUID();

        User stranger = new User();
        stranger.setUsername("stranger");
        stranger.setRoles(new HashSet<>());

        CustomUserDetails principal = new CustomUserDetails(stranger);

        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));
        when(messageResolver.getMessage("comment.delete.access-denied")).thenReturn("denied");

        assertThrows(AccessDeniedException.class, () -> service.delete(id, principal));
        verify(commentRepository, never()).delete(any(Comment.class));
    }

    @Test
    void delete_ShouldThrowEntityNotFoundException_WhenCommentDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findById(id)).thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Comment"), eq(id)))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class,
                () -> service.delete(id, mock(CustomUserDetails.class)));
    }

    // ── getAll ─────────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getAll_ShouldReturnPagedResponse() {
        CommentSpecificationParams specParams = mock(CommentSpecificationParams.class);
        Page<Comment> page = new PageImpl<>(List.of(comment));

        when(commentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(commentMapper.fromEntityToResponse(comment)).thenReturn(commentResponse);

        PagedResponse<CommentResponse> result = service.getAll(defaultPaging, specParams);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
