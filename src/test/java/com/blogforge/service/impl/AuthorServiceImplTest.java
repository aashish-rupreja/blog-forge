package com.blogforge.service.impl;

import com.blogforge.constants.Constants;
import com.blogforge.dto.AuthorProfileResponse;
import com.blogforge.dto.user.UserSummaryResponse;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.UserMapper;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.FollowRepository;
import com.blogforge.repository.UserRepository;
import com.blogforge.specification.user.UserSpecificationParams;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MessageResolver messageResolver;

    @InjectMocks
    private AuthorServiceImpl service;

    private User author;
    private AuthorProfileResponse authorProfileResponse;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setUsername("author1");

        authorProfileResponse = mock(AuthorProfileResponse.class);
    }

    // ── getAuthorProfile ───────────────────────────────────────────────────────

    @Test
    void getAuthorProfile_ShouldReturnProfile_WhenAuthorExists_AndUserIsFollowing() {
        when(userRepository.findByUsernameAndRoles_Name("author1", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.of(author));
        when(followRepository.existsByFollower_UsernameAndFollowing_Username("reader1", "author1"))
                .thenReturn(true);
        when(userMapper.fromEntityToAuthorProfileResponse(author, true))
                .thenReturn(authorProfileResponse);

        AuthorProfileResponse result = service.getAuthorProfile("author1", "reader1");

        assertNotNull(result);
        verify(userMapper).fromEntityToAuthorProfileResponse(author, true);
    }

    @Test
    void getAuthorProfile_ShouldReturnProfile_WhenAuthorExists_AndUserIsNotFollowing() {
        when(userRepository.findByUsernameAndRoles_Name("author1", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.of(author));
        when(followRepository.existsByFollower_UsernameAndFollowing_Username("reader1", "author1"))
                .thenReturn(false);
        when(userMapper.fromEntityToAuthorProfileResponse(author, false))
                .thenReturn(authorProfileResponse);

        AuthorProfileResponse result = service.getAuthorProfile("author1", "reader1");

        assertNotNull(result);
        verify(userMapper).fromEntityToAuthorProfileResponse(author, false);
    }

    @Test
    void getAuthorProfile_ShouldNotCheckFollow_WhenAuthenticatedUsernameIsNull() {
        when(userRepository.findByUsernameAndRoles_Name("author1", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.of(author));
        when(userMapper.fromEntityToAuthorProfileResponse(author, false))
                .thenReturn(authorProfileResponse);

        AuthorProfileResponse result = service.getAuthorProfile("author1", null);

        assertNotNull(result);
        verify(followRepository, never()).existsByFollower_UsernameAndFollowing_Username(any(), any());
        verify(userMapper).fromEntityToAuthorProfileResponse(author, false);
    }

    @Test
    void getAuthorProfile_ShouldThrowEntityNotFoundException_WhenAuthorDoesNotExist() {
        when(userRepository.findByUsernameAndRoles_Name("nobody", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Author"), eq("nobody")))
                .thenReturn("not found");

        assertThrows(
                EntityNotFoundException.class,
                () -> service.getAuthorProfile("nobody", "reader1")
        );
    }

    // ── getMyProfile ────────────────────────────────────────────────────────

    @Test
    void getMyProfile_ShouldReturnProfile_WhenAuthorExists() {
        when(userRepository.findByUsernameAndRoles_Name("author1", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.of(author));
        when(userMapper.fromEntityToAuthorProfileResponse(author, false))
                .thenReturn(authorProfileResponse);

        AuthorProfileResponse result = service.getMyProfile("author1");

        assertNotNull(result);
        // isFollowing is always false for own profile
        verify(userMapper).fromEntityToAuthorProfileResponse(author, false);
    }

    @Test
    void getMyProfile_ShouldThrowEntityNotFoundException_WhenAuthorDoesNotExist() {
        when(userRepository.findByUsernameAndRoles_Name("nobody", Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.empty());
        when(messageResolver.getMessage(eq("entity.not-found"), eq("Author"), eq("nobody")))
                .thenReturn("not found");

        assertThrows(
                EntityNotFoundException.class,
                () -> service.getMyProfile("nobody")
        );
    }

    // ── getAllAuthorSummary ────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void getAllAuthorSummary_ShouldReturnPagedResponse() {
        PaginationRequestParams reqParams = new PaginationRequestParams(1, 10, null, null);
        UserSpecificationParams specParams = mock(UserSpecificationParams.class);

        UserSummaryResponse summaryResponse = mock(UserSummaryResponse.class);
        Page<User> page = new PageImpl<>(List.of(author));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(userMapper.fromEntityToSummaryResponse(author))
                .thenReturn(summaryResponse);

        PagedResponse<UserSummaryResponse> result = service.getAllAuthorSummary(reqParams, specParams);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }
}
