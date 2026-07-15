package com.blogforge.service.impl;

import com.blogforge.constants.Constants;
import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.user.*;
import com.blogforge.entity.Role;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.exception.PasswordMismatchException;
import com.blogforge.mapper.UserMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.RoleRepository;
import com.blogforge.repository.UserRepository;
import com.blogforge.service.UserService;
import com.blogforge.specification.user.UserSpecification;
import com.blogforge.specification.user.UserSpecificationParams;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final MessageResolver messageResolver;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserMapper userMapper,
            MessageResolver messageResolver,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.messageResolver = messageResolver;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PagedResponse<UserSummaryResponse> getAllUserSummary(PaginationRequestParams reqParams, UserSpecificationParams specParams) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<User> userSpec = UserSpecification.handleSpecs(specParams);
        Page<User> users = userRepository.findAll(userSpec, jpaPageable);
        return new PagedResponse<>(
                users.getContent().stream().map(userMapper::fromEntityToSummaryResponse).toList(),
                users.getNumber() + 1,
                users.getNumberOfElements(),
                users.getTotalPages(),
                users.getTotalElements(),
                users.isEmpty(),
                users.hasNext()
        );
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public UserProfileResponse getUserProfile(String username) {
        User user = getUserOrThrow(username);
        return userMapper.fromEntityToUserProfileResponse(user);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public UserProfileResponse create(CreateUserRequest dto) {
        LOG.info("Attempting to register user \"{}\"", dto.username());

        boolean isExistsUsername = userRepository.existsByUsernameIgnoreCase(dto.username());
        if (isExistsUsername) {
            String alreadyExists = messageResolver.getMessage("entity.already-exists", "User with username", dto.username());
            LOG.warn(alreadyExists);
            throw new EntityExistsException(alreadyExists);
        }

        boolean isExistsEmail = userRepository.existsByEmailIgnoreCase(dto.email());
        if (isExistsEmail) {
            String alreadyExists = messageResolver.getMessage("entity.already-exists", "User with email", dto.email());
            LOG.warn(alreadyExists);
            throw new EntityExistsException(alreadyExists);
        }

        User user = userMapper.fromCreateRequestToEntity(dto);

        Role userRole = roleRepository.findByNameIgnoreCase(Constants.USER_ROLE_NAME)
                .orElseThrow(() -> {
                    String notFoundMessage = messageResolver.getMessage("entity.not-found", "Role", Constants.USER_ROLE_NAME);
                    LOG.debug(notFoundMessage);
                    return new EntityNotFoundException(notFoundMessage);
                });
        user.setRoles(Set.of(userRole));
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        User saved = userRepository.save(user);

        LOG.info("User \"{}\" created", saved.getUsername());
        return userMapper.fromEntityToUserProfileResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public UserProfileResponse partialUpdate(UpdateUserRequest dto, String authenticatedUsername) {
        LOG.info("Attempting to update User \"{}\"", authenticatedUsername);

        User toUpdate = getUserOrThrow(authenticatedUsername);

        if (dto.firstName() != null) {
            toUpdate.setFirstName(dto.firstName());
        }
        if (dto.lastName() != null) {
            toUpdate.setLastName(dto.lastName());
        }
        if (dto.username() != null) {
            toUpdate.setUsername(dto.username());
        }
        if (dto.profilePicLink() != null) {
            toUpdate.setProfilePicLink(dto.profilePicLink());
        }
        if (dto.bio() != null) {
            toUpdate.setBio(dto.bio());
        }

        User saved = userRepository.save(toUpdate);
        LOG.info("User \"{}\" updated", saved.getUsername());
        return userMapper.fromEntityToUserProfileResponse(saved);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public GenericResponse changePassword(ChangePasswordRequest dto, String authenticatedUsername) {
        LOG.info("Attempting password update for User \"{}\"", authenticatedUsername);

        User user = getUserOrThrow(authenticatedUsername);

        if (!passwordEncoder.matches(dto.oldPassword(), user.getPasswordHash())) {
            String oldPassMismatch = messageResolver.getMessage("user.password.invalid-old");
            LOG.warn(oldPassMismatch);
            throw new PasswordMismatchException(oldPassMismatch);
        }

        if (!dto.newPassword().equals(dto.confirmNewPassword())) {
            String newPassMismatch = messageResolver.getMessage("user.password.confirmation-mismatch");
            LOG.warn(newPassMismatch);
            throw new PasswordMismatchException(newPassMismatch);
        }

        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
        String pwChanged = messageResolver.getMessage("user.password.changed");
        LOG.info(pwChanged);
        return new GenericResponse(pwChanged);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void assignAuthorRole(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageResolver.getMessage("entity.not-found", "User", userId)
                ));

        Role authorRole = roleRepository.findByNameIgnoreCase(Constants.AUTHOR_ROLE_NAME)
                .orElseThrow(() -> {
                    String notFoundMessage = messageResolver.getMessage("entity.not-found", "Role", Constants.AUTHOR_ROLE_NAME);
                    LOG.debug(notFoundMessage);
                    return new EntityNotFoundException(notFoundMessage);
                });

        Set<Role> roles = user.getRoles();
        roles.add(authorRole);
        user.setRoles(roles);
        userRepository.save(user);
        LOG.info("Granted {} to {}", Constants.AUTHOR_ROLE_NAME, user.getUsername());
    }

    public User getUserOrThrow(String username) {
        LOG.debug("Attempting to fetch user \"{}\"", username);
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> {
                    String notExistsMessage = messageResolver.getMessage("entity.not-found", "User", username);
                    LOG.debug(notExistsMessage);
                    return new EntityNotFoundException(notExistsMessage);
                });
    }
}
