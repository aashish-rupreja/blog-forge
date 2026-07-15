package com.blogforge.service.impl;

import com.blogforge.dto.tag.CreateTagRequest;
import com.blogforge.dto.tag.DeleteTagRequest;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.TagMapper;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.TagRepository;
import com.blogforge.service.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Verifies @PreAuthorize rules on TagServiceImpl.
 *
 * Method → Required role:
 *  getAll    → ROLE_USER
 *  getByName → ROLE_USER
 *  create    → ROLE_AUTHOR
 *  delete    → ROLE_ADMIN
 */
@SpringBootTest(classes = {TagServiceImpl.class, MethodSecurityTestConfig.class})
class TagServiceImplAuthorizationTest {

    @MockitoBean TagRepository tagRepository;
    @MockitoBean TagMapper tagMapper;
    @MockitoBean MessageResolver messageResolver;

    @Autowired TagService tagService;

    private final PaginationRequestParams paging = new PaginationRequestParams(1, 10, null, null);

    // ── getAll — ROLE_USER required ────────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getAll_ShouldBeAccessible_ByUser() {
        when(tagRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
        tagService.getAll(paging, null);
    }

    @Test
    void getAll_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> tagService.getAll(paging, null));
    }

    // ── getByName — ROLE_USER required ────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getByName_ShouldBeAccessible_ByUser() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> tagService.getByName("spring"));
    }

    @Test
    void getByName_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> tagService.getByName("spring"));
    }

    // ── create — ROLE_AUTHOR required ─────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void create_ShouldBeAccessible_ByAuthor() {
        when(tagRepository.findByNameIgnoreCase(anyString())).thenReturn(Optional.empty());
        // Empty → throws EntityExistsException (inverted logic in impl), not AuthorizationDeniedException
        assertThrows(jakarta.persistence.EntityExistsException.class,
                () -> tagService.create(new CreateTagRequest("spring")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void create_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> tagService.create(new CreateTagRequest("spring")));
    }

    @Test
    void create_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> tagService.create(new CreateTagRequest("spring")));
    }

    // ── delete — ROLE_ADMIN required ──────────────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void delete_ShouldBeAccessible_ByAdmin() {
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> tagService.delete(new DeleteTagRequest("spring")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_AUTHOR")
    void delete_ShouldBeDenied_ForAuthor() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> tagService.delete(new DeleteTagRequest("spring")));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void delete_ShouldBeDenied_ForPlainUser() {
        assertThrows(org.springframework.security.authorization.AuthorizationDeniedException.class,
                () -> tagService.delete(new DeleteTagRequest("spring")));
    }

    @Test
    void delete_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> tagService.delete(new DeleteTagRequest("spring")));
    }
}
