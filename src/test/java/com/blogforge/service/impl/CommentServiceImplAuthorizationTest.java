package com.blogforge.service.impl;

import com.blogforge.dto.comment.CreateCommentRequest;
import com.blogforge.dto.comment.UpdateCommentRequest;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.CommentMapper;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.BlogRepository;
import com.blogforge.repository.CommentRepository;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.service.CommentService;
import com.blogforge.specification.comment.CommentSpecificationParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies @PreAuthorize rules on CommentServiceImpl.
 *
 * Method → Required role(s):
 *  getAll        → ROLE_ADMIN
 *  addComment    → ROLE_USER
 *  partialUpdate → ROLE_USER
 *  delete        → ROLE_USER, ROLE_AUTHOR, or ROLE_ADMIN
 */
@SpringBootTest(classes = {CommentServiceImpl.class, MethodSecurityTestConfig.class})
class CommentServiceImplAuthorizationTest {

    @MockitoBean CommentRepository commentRepository;
    @MockitoBean BlogRepository blogRepository;
    @MockitoBean CommentMapper commentMapper;
    @MockitoBean MessageResolver messageResolver;

    @Autowired CommentService commentService;

    private final PaginationRequestParams paging = new PaginationRequestParams(1, 10, null, null);

    // ── getAll — ROLE_ADMIN required ──────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @SuppressWarnings("unchecked")
    void getAll_ShouldBeAccessible_ByAdmin() {
        when(commentRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        commentService.getAll(paging, mock(CommentSpecificationParams.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getAll_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> commentService.getAll(paging, mock(CommentSpecificationParams.class)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void getAll_ShouldBeDenied_ForAuthor() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> commentService.getAll(paging, mock(CommentSpecificationParams.class)));
    }

    @Test
    void getAll_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> commentService.getAll(paging, mock(CommentSpecificationParams.class)));
    }

    // ── addComment — ROLE_USER required ───────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void addComment_ShouldBeAccessible_ByUser() {
        // commentMapper returns null by default → NPE inside method, not auth denial
        try { commentService.addComment("slug", mock(CreateCommentRequest.class), "user1"); }
        catch (org.springframework.security.authorization.AuthorizationDeniedException e) {
            throw new AssertionError("Should not be access-denied for ROLE_USER", e);
        } catch (Exception ignored) { /* expected — no deep mocking */ }
    }

    @Test
    void addComment_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> commentService.addComment("slug", mock(CreateCommentRequest.class), "user1"));
    }

    // ── partialUpdate — ROLE_USER required ────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void partialUpdate_ShouldBeAccessible_ByUser() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> commentService.partialUpdate(UUID.randomUUID(), new UpdateCommentRequest("updated"), "user1"));
    }

    @Test
    void partialUpdate_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> commentService.partialUpdate(UUID.randomUUID(), new UpdateCommentRequest("updated"), "user1"));
    }

    // ── delete — ROLE_USER, ROLE_AUTHOR, or ROLE_ADMIN ───────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void delete_ShouldBeAccessible_ByUser() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> commentService.delete(UUID.randomUUID(), mock(CustomUserDetails.class)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void delete_ShouldBeAccessible_ByAuthor() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> commentService.delete(UUID.randomUUID(), mock(CustomUserDetails.class)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void delete_ShouldBeAccessible_ByAdmin() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> commentService.delete(UUID.randomUUID(), mock(CustomUserDetails.class)));
    }

    @Test
    void delete_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> commentService.delete(UUID.randomUUID(), mock(CustomUserDetails.class)));
    }
}
