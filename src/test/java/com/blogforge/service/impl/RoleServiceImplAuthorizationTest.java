package com.blogforge.service.impl;

import com.blogforge.dto.role.CreateRoleRequest;
import com.blogforge.dto.role.DeleteRoleRequest;
import com.blogforge.entity.Role;
import com.blogforge.entity.RoleType;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.RoleMapper;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.RoleRepository;
import com.blogforge.service.RoleService;
import com.blogforge.specification.role.RoleSpecificationParams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies @PreAuthorize rules on RoleServiceImpl.
 *
 * All methods require ROLE_ADMIN:
 *  getAll, getByName, create, deleteOne, deleteAllIn
 */
@SpringBootTest(classes = {RoleServiceImpl.class, MethodSecurityTestConfig.class})
class RoleServiceImplAuthorizationTest {

    @MockitoBean RoleRepository roleRepository;
    @MockitoBean RoleMapper roleMapper;
    @MockitoBean MessageResolver messageResolver;

    @Autowired RoleService roleService;

    private final PaginationRequestParams paging = new PaginationRequestParams(1, 10, null, null);

    // ── getAll — ROLE_ADMIN required ──────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    @SuppressWarnings("unchecked")
    void getAll_ShouldBeAccessible_ByAdmin() {
        when(roleRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        roleService.getAll(paging, mock(RoleSpecificationParams.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getAll_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> roleService.getAll(paging, mock(RoleSpecificationParams.class)));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void getAll_ShouldBeDenied_ForAuthor() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> roleService.getAll(paging, mock(RoleSpecificationParams.class)));
    }

    @Test
    void getAll_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> roleService.getAll(paging, mock(RoleSpecificationParams.class)));
    }

    // ── getByName — ROLE_ADMIN required ──────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getByName_ShouldBeAccessible_ByAdmin() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> roleService.getByName("ROLE_EDITOR"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getByName_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> roleService.getByName("ROLE_EDITOR"));
    }

    @Test
    void getByName_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> roleService.getByName("ROLE_EDITOR"));
    }

    // ── create — ROLE_ADMIN required ─────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void create_ShouldBeAccessible_ByAdmin() {
        Role mockRole = mock(Role.class);
        when(roleMapper.fromCreateRequestToEntity(any())).thenReturn(mockRole);
        when(roleRepository.save(any())).thenReturn(mockRole);
        roleService.create(new CreateRoleRequest("editor", RoleType.CUSTOM));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void create_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> roleService.create(new CreateRoleRequest("editor", RoleType.CUSTOM)));
    }

    @Test
    void create_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> roleService.create(new CreateRoleRequest("editor", RoleType.CUSTOM)));
    }

    // ── deleteOne — ROLE_ADMIN required ──────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteOne_ShouldBeAccessible_ByAdmin() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> roleService.deleteOne("ROLE_EDITOR"));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void deleteOne_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> roleService.deleteOne("ROLE_EDITOR"));
    }

    @Test
    void deleteOne_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> roleService.deleteOne("ROLE_EDITOR"));
    }

    // ── deleteAllIn — ROLE_ADMIN required ────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void deleteAllIn_ShouldBeAccessible_ByAdmin() {
        when(roleRepository.deleteAllIn(any())).thenReturn(1L);
        when(messageResolver.getMessage(anyString(), anyString(), anyString())).thenReturn("deleted");
        roleService.deleteAllIn(new DeleteRoleRequest(Set.of("editor")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void deleteAllIn_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> roleService.deleteAllIn(new DeleteRoleRequest(Set.of("editor"))));
    }

    @Test
    void deleteAllIn_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> roleService.deleteAllIn(new DeleteRoleRequest(Set.of("editor"))));
    }
}
