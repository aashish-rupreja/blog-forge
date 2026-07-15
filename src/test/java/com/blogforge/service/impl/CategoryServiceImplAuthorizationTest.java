package com.blogforge.service.impl;

import com.blogforge.dto.category.CreateCategoryRequest;
import com.blogforge.dto.category.DeleteCategoryRequest;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.CategoryMapper;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.CategoryRepository;
import com.blogforge.service.CategoryService;
import com.blogforge.specification.category.CategorySpecificationParams;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies @PreAuthorize rules on CategoryServiceImpl.
 *
 * Method → Required role:
 *  getAll    → ROLE_USER
 *  getByName → ROLE_USER
 *  create    → ROLE_ADMIN
 *  delete    → ROLE_ADMIN
 */
@SpringBootTest(classes = {CategoryServiceImpl.class, MethodSecurityTestConfig.class})
class CategoryServiceImplAuthorizationTest {

    @MockitoBean CategoryRepository categoryRepository;
    @MockitoBean CategoryMapper categoryMapper;
    @MockitoBean MessageResolver messageResolver;

    @Autowired CategoryService categoryService;

    private final PaginationRequestParams paging = new PaginationRequestParams(1, 10, null, null);

    // ── getAll — ROLE_USER required ────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    @SuppressWarnings("unchecked")
    void getAll_ShouldBeAccessible_ByUser() {
        when(categoryRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        categoryService.getAll(paging, mock(CategorySpecificationParams.class));
    }

    @Test
    void getAll_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> categoryService.getAll(paging, mock(CategorySpecificationParams.class)));
    }

    // ── getByName — ROLE_USER required ────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getByName_ShouldBeAccessible_ByUser() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> categoryService.getByName("Technology"));
    }

    @Test
    void getByName_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> categoryService.getByName("Technology"));
    }

    // ── create — ROLE_ADMIN required ──────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void create_ShouldBeAccessible_ByAdmin() {
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        // Empty → throws EntityExistsException (inverted logic in impl), but NOT AuthorizationDeniedException
        assertThrows(jakarta.persistence.EntityExistsException.class,
                () -> categoryService.create(new CreateCategoryRequest("Tech")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void create_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> categoryService.create(new CreateCategoryRequest("Tech")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void create_ShouldBeDenied_ForAuthor() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> categoryService.create(new CreateCategoryRequest("Tech")));
    }

    @Test
    void create_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> categoryService.create(new CreateCategoryRequest("Tech")));
    }

    // ── delete — ROLE_ADMIN required ──────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void delete_ShouldBeAccessible_ByAdmin() {
        when(categoryRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        // Throws EntityExistsException from inverted orElseThrow, but NOT AuthorizationDeniedException
        assertThrows(jakarta.persistence.EntityExistsException.class,
                () -> categoryService.delete(new DeleteCategoryRequest("Tech")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void delete_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> categoryService.delete(new DeleteCategoryRequest("Tech")));
    }

    @Test
    void delete_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> categoryService.delete(new DeleteCategoryRequest("Tech")));
    }
}
