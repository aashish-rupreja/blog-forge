package com.blogforge.service.impl;

import com.blogforge.constants.Constants;
import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.user.ChangePasswordRequest;
import com.blogforge.dto.user.CreateUserRequest;
import com.blogforge.dto.user.UserProfileResponse;
import com.blogforge.entity.Role;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.exception.PasswordMismatchException;
import com.blogforge.mapper.UserMapper;
import com.blogforge.repository.RoleRepository;
import com.blogforge.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private MessageResolver messageResolver;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl service;

    private User user;
    private Role userRole;
    private CreateUserRequest createRequest;
    private UserProfileResponse response;

    @BeforeEach
    void setUp() {

        userRole = new Role();
        userRole.setName(Constants.USER_ROLE_NAME);

        user = new User();
        user.setUsername("john");
        user.setEmail("john@test.com");
        user.setPasswordHash("rawPassword");
        user.setRoles(new HashSet<>());

        createRequest = new CreateUserRequest(
                "John",
                "Doe",
                "john",
                null,
                null,
                "john@test.com",
                "Password@123"
        );

        response = mock(UserProfileResponse.class);
    }

    @Test
    void create_ShouldRegisterUser_WhenUsernameAndEmailAreUnique() {

        when(userRepository.existsByUsernameIgnoreCase("john")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("john@test.com")).thenReturn(false);

        when(userMapper.fromCreateRequestToEntity(createRequest)).thenReturn(user);

        when(roleRepository.findByNameIgnoreCase(Constants.USER_ROLE_NAME))
                .thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode("rawPassword"))
                .thenReturn("encodedPassword");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userMapper.fromEntityToUserProfileResponse(any(User.class)))
                .thenReturn(response);

        UserProfileResponse result = service.create(createRequest);

        assertNotNull(result);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();

        assertEquals("encodedPassword", saved.getPasswordHash());
        assertTrue(saved.getRoles().contains(userRole));
    }

    @Test
    void create_ShouldThrowEntityExistsException_WhenUsernameAlreadyExists() {

        when(userRepository.existsByUsernameIgnoreCase("john")).thenReturn(true);

        when(messageResolver.getMessage(
                eq("entity.already-exists"),
                eq("User with username"),
                eq("john")
        )).thenReturn("exists");

        assertThrows(
                EntityExistsException.class,
                () -> service.create(createRequest)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void create_ShouldThrowEntityExistsException_WhenEmailAlreadyExists() {

        when(userRepository.existsByUsernameIgnoreCase("john")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase("john@test.com")).thenReturn(true);

        when(messageResolver.getMessage(
                eq("entity.already-exists"),
                eq("User with email"),
                eq("john@test.com")
        )).thenReturn("exists");

        assertThrows(
                EntityExistsException.class,
                () -> service.create(createRequest)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_ShouldUpdatePassword_WhenOldPasswordMatches() {

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "oldPassword",
                        "newPassword",
                        "newPassword"
                );

        when(userRepository.findByUsernameIgnoreCase("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("oldPassword", "rawPassword"))
                .thenReturn(true);

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("encoded");

        when(messageResolver.getMessage("user.password.changed"))
                .thenReturn("Password changed");

        GenericResponse response =
                service.changePassword(request, "john");

        assertEquals("Password changed", response.message());
        assertEquals("encoded", user.getPasswordHash());

        verify(userRepository).save(user);
    }

    @Test
    void changePassword_ShouldThrowPasswordMismatchException_WhenOldPasswordDoesNotMatch() {

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "wrong",
                        "new",
                        "new"
                );

        when(userRepository.findByUsernameIgnoreCase("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "rawPassword"))
                .thenReturn(false);

        when(messageResolver.getMessage("user.password.invalid-old"))
                .thenReturn("invalid");

        assertThrows(
                PasswordMismatchException.class,
                () -> service.changePassword(request, "john")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_ShouldThrowPasswordMismatchException_WhenConfirmationDoesNotMatch() {

        ChangePasswordRequest request =
                new ChangePasswordRequest(
                        "old",
                        "new1",
                        "new2"
                );

        when(userRepository.findByUsernameIgnoreCase("john"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("old", "rawPassword"))
                .thenReturn(true);

        when(messageResolver.getMessage("user.password.confirmation-mismatch"))
                .thenReturn("mismatch");

        assertThrows(
                PasswordMismatchException.class,
                () -> service.changePassword(request, "john")
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void assignAuthorRole_ShouldAssignAuthorRole() {

        Role authorRole = new Role();
        authorRole.setName(Constants.AUTHOR_ROLE_NAME);

        user.setRoles(new HashSet<>());

        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.of(user));

        when(roleRepository.findByNameIgnoreCase(Constants.AUTHOR_ROLE_NAME))
                .thenReturn(Optional.of(authorRole));

        service.assignAuthorRole(id);

        assertTrue(user.getRoles().contains(authorRole));

        verify(userRepository).save(user);
    }

    @Test
    void assignAuthorRole_ShouldThrowEntityNotFound_WhenUserDoesNotExist() {

        UUID id = UUID.randomUUID();

        when(userRepository.findById(id))
                .thenReturn(Optional.empty());

        when(messageResolver.getMessage(
                eq("entity.not-found"),
                eq("User"),
                eq(id)
        )).thenReturn("not found");

        assertThrows(
                EntityNotFoundException.class,
                () -> service.assignAuthorRole(id)
        );
    }
}