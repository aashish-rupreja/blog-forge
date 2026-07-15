package com.blogforge.service.impl;

import com.blogforge.constants.Constants;
import com.blogforge.dto.GenericResponse;
import com.blogforge.entity.Follow;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.repository.FollowRepository;
import com.blogforge.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private FollowServiceImpl service;

    private User author;
    private User followerUser;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setUsername("authorUser");

        followerUser = new User();
        followerUser.setUsername("followerUser");
    }

    // ── create (follow) ────────────────────────────────────────────────────────

    @Test
    void create_ShouldCreateFollow_WhenAllConditionsMet() {
        when(userRepository.findByUsernameAndRoles_Name("authorUser", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.of(author));
        when(followRepository.existsByFollower_UsernameAndFollowing_Username("followerUser", "authorUser"))
                .thenReturn(false);
        when(userRepository.findByUsernameIgnoreCase("followerUser"))
                .thenReturn(Optional.of(followerUser));
        when(messageResolver.getMessage("follow.created", "authorUser"))
                .thenReturn("Now following authorUser");

        GenericResponse result = service.create("authorUser", "followerUser");

        assertNotNull(result);
        assertEquals("Now following authorUser", result.message());

        ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
        verify(followRepository).save(captor.capture());
        Follow savedFollow = captor.getValue();
        assertEquals(author, savedFollow.getFollowing());
        assertEquals(followerUser, savedFollow.getFollower());
    }

    @Test
    void create_ShouldThrowIllegalStateException_WhenUserTriesToFollowThemself() {
        when(messageResolver.getMessage("follow.self.not-allowed"))
                .thenReturn("Cannot follow yourself");

        assertThrows(IllegalStateException.class,
                () -> service.create("sameUser", "sameUser"));
        verify(followRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowEntityNotFoundException_WhenAuthorDoesNotExist() {
        when(userRepository.findByUsernameAndRoles_Name("nobody", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Author"), eq("nobody")))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class,
                () -> service.create("nobody", "followerUser"));
        verify(followRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowIllegalStateException_WhenAlreadyFollowing() {
        when(userRepository.findByUsernameAndRoles_Name("authorUser", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.of(author));
        when(followRepository.existsByFollower_UsernameAndFollowing_Username("followerUser", "authorUser"))
                .thenReturn(true);
        when(messageResolver.getMessage("follow.already-following", "authorUser"))
                .thenReturn("Already following");

        assertThrows(IllegalStateException.class,
                () -> service.create("authorUser", "followerUser"));
        verify(followRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowEntityNotFoundException_WhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findByUsernameAndRoles_Name("authorUser", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.of(author));
        when(followRepository.existsByFollower_UsernameAndFollowing_Username("ghost", "authorUser"))
                .thenReturn(false);
        when(userRepository.findByUsernameIgnoreCase("ghost"))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("User"), anyString()))
                .thenReturn("not found");

        assertThrows(EntityNotFoundException.class,
                () -> service.create("authorUser", "ghost"));
        verify(followRepository, never()).save(any());
    }

    // ── delete (unfollow) ──────────────────────────────────────────────────────

    @Test
    void delete_ShouldUnfollow_WhenFollowExists() {
        when(followRepository.deleteByFollower_UsernameAndFollowing_Username("followerUser", "authorUser"))
                .thenReturn(1);
        when(messageResolver.getMessage("follow.removed", "authorUser"))
                .thenReturn("Unfollowed authorUser");

        GenericResponse result = service.delete("authorUser", "followerUser");

        assertNotNull(result);
        assertEquals("Unfollowed authorUser", result.message());
    }

    @Test
    void delete_ShouldReturnNotFollowingMessage_WhenNoFollowExists() {
        when(followRepository.deleteByFollower_UsernameAndFollowing_Username("followerUser", "authorUser"))
                .thenReturn(0);
        when(messageResolver.getMessage("unfollow.not-following", "authorUser"))
                .thenReturn("Not following authorUser");

        GenericResponse result = service.delete("authorUser", "followerUser");

        assertNotNull(result);
        assertEquals("Not following authorUser", result.message());
    }

    @Test
    void delete_ShouldThrowIllegalStateException_WhenUserTriesToUnfollowThemself() {
        when(messageResolver.getMessage("unfollow.self.not-allowed"))
                .thenReturn("Cannot unfollow yourself");

        assertThrows(IllegalStateException.class,
                () -> service.delete("sameUser", "sameUser"));
        verify(followRepository, never()).deleteByFollower_UsernameAndFollowing_Username(any(), any());
    }
}
