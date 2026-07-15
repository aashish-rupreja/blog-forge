package com.blogforge.service.impl;

import com.blogforge.exception.MessageResolver;
import com.blogforge.repository.FollowRepository;
import com.blogforge.repository.UserRepository;
import com.blogforge.service.FollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Verifies @PreAuthorize rules on FollowServiceImpl.
 *
 * Method → Required role:
 *  create (follow)   → ROLE_USER
 *  delete (unfollow) → ROLE_USER
 */
@SpringBootTest(classes = {FollowServiceImpl.class, MethodSecurityTestConfig.class})
class FollowServiceImplAuthorizationTest {

    @MockitoBean FollowRepository followRepository;
    @MockitoBean UserRepository userRepository;
    @MockitoBean MessageResolver messageResolver;

    @Autowired FollowService followService;

    // ── create (follow) — ROLE_USER required ──────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER", username = "followerUser")
    void create_ShouldBeAccessible_ByUser() {
        // Passes auth; the self-follow check doesn't fire (different usernames),
        // then orElseThrow throws EntityNotFoundException — that's OK
        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> followService.create("authorUser", "followerUser"));
    }

    @Test
    void create_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> followService.create("authorUser", "followerUser"));
    }

    // ── delete (unfollow) — ROLE_USER required ────────────────────────────────

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void delete_ShouldBeAccessible_ByUser() {
        when(followRepository.deleteByFollower_UsernameAndFollowing_Username(anyString(), anyString()))
                .thenReturn(0);
        when(messageResolver.getMessage(anyString(), anyString())).thenReturn("not following");
        followService.delete("authorUser", "followerUser");
    }

    @Test
    void delete_ShouldBeDenied_WhenUnauthenticated() {
        assertThrows(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class,
                () -> followService.delete("authorUser", "followerUser"));
    }
}
