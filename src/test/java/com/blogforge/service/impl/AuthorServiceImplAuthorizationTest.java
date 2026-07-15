package com.blogforge.service.impl;

import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.UserMapper;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.FollowRepository;
import com.blogforge.repository.UserRepository;
import com.blogforge.service.AuthorService;
import com.blogforge.specification.user.UserSpecificationParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies @PreAuthorize rules on AuthorServiceImpl.
 *
 * Method → Required role:
 *  getAllAuthorSummary → ROLE_USER
 *  getAuthorProfile   → ROLE_USER
 *  getMyProfile       → ROLE_AUTHOR
 */
@SpringBootTest(classes = {AuthorServiceImpl.class, MethodSecurityTestConfig.class})
class AuthorServiceImplAuthorizationTest {

    @MockitoBean UserRepository userRepository;
    @MockitoBean FollowRepository followRepository;
    @MockitoBean UserMapper userMapper;
    @MockitoBean MessageResolver messageResolver;

    @Autowired AuthorService authorService;

    private final PaginationRequestParams paging = new PaginationRequestParams(1, 10, null, null);

    // ── getAllAuthorSummary — ROLE_USER required ───────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    @SuppressWarnings("unchecked")
    void getAllAuthorSummary_ShouldBeAccessible_ByUser() {
        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        authorService.getAllAuthorSummary(paging, mock(UserSpecificationParams.class));
    }

    @Test
    void getAllAuthorSummary_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> authorService.getAllAuthorSummary(paging, mock(UserSpecificationParams.class)));
    }

    // ── getAuthorProfile — ROLE_USER required ─────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getAuthorProfile_ShouldBeAccessible_ByUser() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> authorService.getAuthorProfile("author1", "viewer"));
    }

    @Test
    void getAuthorProfile_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> authorService.getAuthorProfile("author1", "viewer"));
    }

    // ── getMyProfile — ROLE_AUTHOR required ───────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void getMyProfile_ShouldBeAccessible_ByAuthor() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> authorService.getMyProfile("author1"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getMyProfile_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> authorService.getMyProfile("user1"));
    }

    @Test
    void getMyProfile_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> authorService.getMyProfile("author1"));
    }
}
