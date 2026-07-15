package com.blogforge.service.impl;

import com.blogforge.dto.blog.CreateBlogRequest;
import com.blogforge.dto.blog.UpdateBlogRequest;
import com.blogforge.dto.reaction.AddReactionRequest;
import com.blogforge.mapper.BlogMapper;
import com.blogforge.mapper.CommentMapper;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.*;
import com.blogforge.security.CustomUserDetails;
import com.blogforge.service.BlogService;
import com.blogforge.specification.blog.BlogSpecificationParams;
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
 * Verifies @PreAuthorize rules on BlogServiceImpl.
 *
 * Method → Required role(s):
 *  getAllSummary   → ROLE_USER
 *  getBlogDetails → ROLE_USER
 *  getBlogComments→ ROLE_USER
 *  partialUpdate  → ROLE_AUTHOR
 *  delete         → ROLE_AUTHOR or ROLE_ADMIN
 *  getMyBlogs     → ROLE_AUTHOR
 *  create         → ROLE_AUTHOR
 *  hardDelete     → ROLE_ADMIN
 *  like           → ROLE_USER
 */
@SpringBootTest(classes = {BlogServiceImpl.class, MethodSecurityTestConfig.class})
class BlogServiceImplAuthorizationTest {

    @MockitoBean BlogRepository blogRepository;
    @MockitoBean CommentRepository commentRepository;
    @MockitoBean CategoryRepository categoryRepository;
    @MockitoBean TagRepository tagRepository;
    @MockitoBean UserRepository userRepository;
    @MockitoBean ReactionRepository reactionRepository;
    @MockitoBean BlogMapper blogMapper;
    @MockitoBean CommentMapper commentMapper;
    @MockitoBean com.blogforge.exception.MessageResolver messageResolver;

    @Autowired BlogService blogService;

    private final PaginationRequestParams paging = new PaginationRequestParams(1, 10, null, null);

    // ── getAllSummary — ROLE_USER required ─────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    @SuppressWarnings("unchecked")
    void getAllSummary_ShouldBeAccessible_ByUser() {
        when(blogRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        blogService.getAllSummary(paging, mock(BlogSpecificationParams.class));
    }

    @Test
    void getAllSummary_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> blogService.getAllSummary(paging, mock(BlogSpecificationParams.class)));
    }

    // ── getBlogDetails — ROLE_USER required ───────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getBlogDetails_ShouldBeAccessible_ByUser() {
        // EntityNotFoundException is expected — but not AuthorizationDeniedException
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> blogService.getBlogDetails("any-slug"));
    }

    @Test
    void getBlogDetails_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> blogService.getBlogDetails("any-slug"));
    }

    // ── partialUpdate — ROLE_AUTHOR required ──────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void partialUpdate_ShouldBeAccessible_ByAuthor() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> blogService.partialUpdate("slug", mock(UpdateBlogRequest.class), mock(CustomUserDetails.class)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void partialUpdate_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> blogService.partialUpdate("slug", mock(UpdateBlogRequest.class), mock(CustomUserDetails.class)));
    }

    @Test
    void partialUpdate_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> blogService.partialUpdate("slug", mock(UpdateBlogRequest.class), mock(CustomUserDetails.class)));
    }

    // ── delete — ROLE_AUTHOR or ROLE_ADMIN required ───────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void delete_ShouldBeAccessible_ByAuthor() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> blogService.delete("slug"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void delete_ShouldBeAccessible_ByAdmin() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> blogService.delete("slug"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void delete_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> blogService.delete("slug"));
    }

    // ── getMyBlogs — ROLE_AUTHOR required ─────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void getMyBlogs_ShouldBeAccessible_ByAuthor() {
        when(blogRepository.findAllByAuthor_UsernameIgnoreCase(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        blogService.getMyBlogs(paging, "author1");
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getMyBlogs_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> blogService.getMyBlogs(paging, "user1"));
    }

    // ── create — ROLE_AUTHOR required ─────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void create_ShouldBeAccessible_ByAuthor() {
        // NullPointerException or EntityNotFoundException from inside, not auth denial
        try { blogService.create(mock(CreateBlogRequest.class), "author1"); }
        catch (org.springframework.security.authorization.AuthorizationDeniedException e) {
            throw new AssertionError("Should not be access-denied for ROLE_AUTHOR", e);
        } catch (Exception ignored) { /* expected — no mocks wired */ }
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void create_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> blogService.create(mock(CreateBlogRequest.class), "user1"));
    }

    // ── hardDelete — ROLE_ADMIN required ──────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void hardDelete_ShouldBeAccessible_ByAdmin() {
        blogService.hardDelete(List.of(UUID.randomUUID()));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void hardDelete_ShouldBeDenied_ForAuthor() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> blogService.hardDelete(List.of(UUID.randomUUID())));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void hardDelete_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> blogService.hardDelete(List.of(UUID.randomUUID())));
    }

    // ── like — ROLE_USER required ──────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void like_ShouldBeAccessible_ByUser() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> blogService.like("slug", mock(AddReactionRequest.class), mock(CustomUserDetails.class)));
    }

    @Test
    void like_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> blogService.like("slug", mock(AddReactionRequest.class), mock(CustomUserDetails.class)));
    }
}
