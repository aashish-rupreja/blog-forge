package com.blogforge.service.impl;

import com.blogforge.dto.authorapplication.CreateAuthorApplicationRequest;
import com.blogforge.dto.authorapplication.MyAuthorApplicationsRequest;
import com.blogforge.dto.authorapplication.UpdateAuthorApplicationRequest;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.AuthorApplicationMapper;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.AuthorApplicationRepository;
import com.blogforge.service.AuthorApplicationService;
import com.blogforge.service.UserService;
import com.blogforge.specification.authorapplication.AuthorApplicationSpecificationParams;
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
 * Verifies @PreAuthorize rules on AuthorApplicationServiceImpl.
 *
 * Method → Required role:
 *  getAll                  → ROLE_ADMIN
 *  getMyAuthorApplications → ROLE_USER
 *  create                  → ROLE_USER
 *  getSingleApplication    → ROLE_USER
 *  approveApplication      → ROLE_ADMIN
 *  rejectApplication       → ROLE_ADMIN
 */
@SpringBootTest(classes = {AuthorApplicationServiceImpl.class, MethodSecurityTestConfig.class})
class AuthorApplicationServiceImplAuthorizationTest {

    @MockitoBean AuthorApplicationRepository authorApplicationRepository;
    @MockitoBean UserService userService;
    @MockitoBean AuthorApplicationMapper authorApplicationMapper;
    @MockitoBean MessageResolver messageResolver;

    @Autowired AuthorApplicationService authorApplicationService;

    private final PaginationRequestParams paging = new PaginationRequestParams(1, 10, null, null);

    // ── getAll — ROLE_ADMIN required ──────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @SuppressWarnings("unchecked")
    void getAll_ShouldBeAccessible_ByAdmin() {
        when(authorApplicationRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        authorApplicationService.getAll(paging, mock(AuthorApplicationSpecificationParams.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getAll_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> authorApplicationService.getAll(paging, mock(AuthorApplicationSpecificationParams.class)));
    }

    @Test
    void getAll_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> authorApplicationService.getAll(paging, mock(AuthorApplicationSpecificationParams.class)));
    }

    // ── getMyAuthorApplications — ROLE_USER required ──────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    @SuppressWarnings("unchecked")
    void getMyAuthorApplications_ShouldBeAccessible_ByUser() {
        when(authorApplicationRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        authorApplicationService.getMyAuthorApplications(paging, mock(MyAuthorApplicationsRequest.class), "user1");
    }

    @Test
    void getMyAuthorApplications_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> authorApplicationService.getMyAuthorApplications(paging, mock(MyAuthorApplicationsRequest.class), "user1"));
    }

    // ── create — ROLE_USER required ───────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void create_ShouldBeAccessible_ByUser() {
        try { authorApplicationService.create(mock(CreateAuthorApplicationRequest.class), "user1"); }
        catch (org.springframework.security.authorization.AuthorizationDeniedException e) {
            throw new AssertionError("Should not be access-denied for ROLE_USER", e);
        } catch (Exception ignored) { /* expected — no deep mocking */ }
    }

    @Test
    void create_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> authorApplicationService.create(mock(CreateAuthorApplicationRequest.class), "user1"));
    }

    // ── getSingleApplication — ROLE_USER required ─────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getSingleApplication_ShouldBeAccessible_ByUser() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> authorApplicationService.getSingleApplication(UUID.randomUUID()));
    }

    @Test
    void getSingleApplication_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> authorApplicationService.getSingleApplication(UUID.randomUUID()));
    }

    // ── approveApplication — ROLE_ADMIN required ──────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void approveApplication_ShouldBeAccessible_ByAdmin() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> authorApplicationService.approveApplication(UUID.randomUUID(),
                        new UpdateAuthorApplicationRequest(null, "Approved")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void approveApplication_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> authorApplicationService.approveApplication(UUID.randomUUID(),
                        new UpdateAuthorApplicationRequest(null, "Approved")));
    }

    @Test
    void approveApplication_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> authorApplicationService.approveApplication(UUID.randomUUID(),
                        new UpdateAuthorApplicationRequest(null, "Approved")));
    }

    // ── rejectApplication — ROLE_ADMIN required ───────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void rejectApplication_ShouldBeAccessible_ByAdmin() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> authorApplicationService.rejectApplication(UUID.randomUUID(),
                        new UpdateAuthorApplicationRequest(null, "Rejected")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void rejectApplication_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> authorApplicationService.rejectApplication(UUID.randomUUID(),
                        new UpdateAuthorApplicationRequest(null, "Rejected")));
    }

    @Test
    void rejectApplication_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> authorApplicationService.rejectApplication(UUID.randomUUID(),
                        new UpdateAuthorApplicationRequest(null, "Rejected")));
    }
}
