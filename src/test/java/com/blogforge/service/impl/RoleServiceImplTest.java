package com.blogforge.service.impl;

import com.blogforge.constants.Constants;
import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.role.CreateRoleRequest;
import com.blogforge.dto.role.DeleteRoleRequest;
import com.blogforge.dto.role.RoleResponse;
import com.blogforge.entity.Role;
import com.blogforge.entity.RoleType;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.RoleMapper;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.RoleRepository;
import com.blogforge.specification.role.RoleSpecificationParams;
import jakarta.persistence.EntityExistsException;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private RoleServiceImpl service;

    private Role customRole;
    private Role systemRole;
    private RoleResponse roleResponse;
    private PaginationRequestParams defaultPaging;

    @BeforeEach
    void setUp() {
        customRole = new Role();
        customRole.setName("ROLE_EDITOR");
        customRole.setRoleType(RoleType.CUSTOM);

        systemRole = new Role();
        systemRole.setName(Constants.ADMIN_ROLE_NAME);
        systemRole.setRoleType(RoleType.SYSTEM);

        roleResponse = mock(RoleResponse.class);
        defaultPaging = new PaginationRequestParams(1, 10, null, null);
    }

    // ── getByName ──────────────────────────────────────────────────────────────

    @Test
    void getByName_ShouldReturnRole_WhenRoleExists() {
        when(roleRepository.findByNameIgnoreCase("ROLE_EDITOR"))
                .thenReturn(Optional.of(customRole));
        when(roleMapper.fromEntityToResponse(customRole))
                .thenReturn(roleResponse);

        RoleResponse result = service.getByName("ROLE_EDITOR");

        assertNotNull(result);
    }

    @Test
    void getByName_ShouldThrowEntityNotFoundException_WhenRoleDoesNotExist() {
        when(roleRepository.findByNameIgnoreCase("ROLE_UNKNOWN"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Role"), eq("ROLE_UNKNOWN")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class, () -> service.getByName("ROLE_UNKNOWN"));
    }

    // ── create ─────────────────────────────────────────────────────────────────

    @Test
    void create_ShouldCreateRole_WhenRoleDoesNotExist() {
        CreateRoleRequest dto = new CreateRoleRequest("editor", RoleType.CUSTOM);

        when(roleRepository.existsByNameIgnoreCase("EDITOR")).thenReturn(false);
        when(roleMapper.fromCreateRequestToEntity(any(CreateRoleRequest.class)))
                .thenReturn(customRole);
        when(roleRepository.save(any(Role.class))).thenReturn(customRole);
        when(roleMapper.fromEntityToResponse(customRole)).thenReturn(roleResponse);

        RoleResponse result = service.create(dto);

        assertNotNull(result);
        verify(roleRepository).save(any(Role.class));
        // Role type must be set to CUSTOM regardless of input
        assertEquals(RoleType.CUSTOM, customRole.getRoleType());
    }

    @Test
    void create_ShouldThrowEntityExistsException_WhenRoleAlreadyExists() {
        CreateRoleRequest dto = new CreateRoleRequest("editor", RoleType.CUSTOM);

        when(roleRepository.existsByNameIgnoreCase("EDITOR")).thenReturn(true);
        when(messageResolver.getMessage(eq("entity.already-exists"), eq("Role"), anyString()))
                .thenReturn("already exists");

        assertThrows(EntityExistsException.class, () -> service.create(dto));
        verify(roleRepository, never()).save(any());
    }

    // ── deleteOne ─────────────────────────────────────────────────────────────

    @Test
    void deleteOne_ShouldDeleteRole_WhenRoleIsCustom() {
        when(roleRepository.findByNameIgnoreCase("ROLE_EDITOR"))
                .thenReturn(Optional.of(customRole));
        when(messageResolver.getMessage(eq("entity.delete.success"), anyString(), anyString()))
                .thenReturn("Deleted");

        GenericResponse result = service.deleteOne("ROLE_EDITOR");

        assertNotNull(result);
        verify(roleRepository).delete((Role) customRole);
    }

    @Test
    void deleteOne_ShouldThrowIllegalArgumentException_WhenRoleIsSystem() {
        when(roleRepository.findByNameIgnoreCase(Constants.ADMIN_ROLE_NAME))
                .thenReturn(Optional.of(systemRole));
        when(messageResolver.getMessage(eq("role.system.delete.not-allowed"), anyString()))
                .thenReturn("cannot delete system role");

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteOne(Constants.ADMIN_ROLE_NAME));
        verify(roleRepository, never()).delete(any(Role.class));
    }

    @Test
    void deleteOne_ShouldThrowEntityNotFoundException_WhenRoleDoesNotExist() {
        when(roleRepository.findByNameIgnoreCase("ROLE_GHOST"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Role"), eq("ROLE_GHOST")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class, () -> service.deleteOne("ROLE_GHOST"));
    }

    // ── deleteAllIn ───────────────────────────────────────────────────────────

    @Test
    void deleteAllIn_ShouldDeleteCustomRoles() {
        DeleteRoleRequest dto = new DeleteRoleRequest(Set.of("editor"));

        when(roleRepository.deleteAllIn(anySet())).thenReturn(1L);
        when(messageResolver.getMessage(eq("entity.delete.success"), anyString(), anyString()))
                .thenReturn("1 Roles deleted");

        GenericResponse result = service.deleteAllIn(dto);

        assertNotNull(result);
        verify(roleRepository).deleteAllIn(anySet());
    }

    @Test
    void deleteAllIn_ShouldThrowIllegalArgumentException_WhenSystemRoleIncluded() {
        // ROLE_ADMIN is a system role
        DeleteRoleRequest dto = new DeleteRoleRequest(Set.of(Constants.ADMIN_ROLE_NAME));
        when(messageResolver.getMessage(eq("role.system.delete.not-allowed"), anyString()))
                .thenReturn("cannot delete system role");

        assertThrows(IllegalArgumentException.class, () -> service.deleteAllIn(dto));
        verify(roleRepository, never()).deleteAllIn(any());
    }

    @Test
    void deleteAllIn_ShouldThrowIllegalArgumentException_WhenEmptySetProvided() {
        // After normalizing, if nothing to delete
        DeleteRoleRequest dto = new DeleteRoleRequest(Set.of());
        when(messageResolver.getMessage(eq("entity.to-delete.not-provided"), anyString()))
                .thenReturn("nothing to delete");

        assertThrows(IllegalArgumentException.class, () -> service.deleteAllIn(dto));
        verify(roleRepository, never()).deleteAllIn(any());
    }

    // ── getAll ─────────────────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getAll_ShouldReturnPagedResponse() {
        RoleSpecificationParams specParams = mock(RoleSpecificationParams.class);
        Page<Role> page = new PageImpl<>(List.of(customRole));

        when(roleRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(roleMapper.fromEntityToResponse(customRole)).thenReturn(roleResponse);

        PagedResponse<RoleResponse> result = service.getAll(defaultPaging, specParams);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
