package com.blogforge.service.impl;

import com.blogforge.dto.user.CreateUserRequest;
import com.blogforge.dto.user.UserProfileResponse;
import com.blogforge.dto.user.UserSummaryResponse;
import com.blogforge.entity.Role;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private final String USER_ROLE_NAME = "ROLE_USER";

    private final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

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
    public PagedResponse<UserSummaryResponse> getAllUserSummary(PaginationRequestParams reqParams, UserSpecificationParams specParams) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<User> userSpec = UserSpecification.handleSpecs(specParams);
        Page<User> users = userRepository.findAll(userSpec, jpaPageable);
        return new PagedResponse<>(
                users.getContent().stream().map(userMapper::fromEntityToSummaryResponse).toList(),
                users.getNumber()+1,
                users.getNumberOfElements(),
                users.getTotalPages(),
                users.getTotalElements(),
                users.isEmpty(),
                users.hasNext()
        );
    }

    @Override
    public UserProfileResponse getUserProfile(String username) {
        LOG.debug("Attempting to fetch profile for \"{}\"", username);
        Optional<User> user = userRepository.findByUsernameIgnoreCase(username);
        if(user.isEmpty()) {
            String notExistsMessage = messageResolver.getMessage("entity.not-found", "User", username);
            LOG.debug(notExistsMessage);
            throw new EntityNotFoundException(notExistsMessage);
        }
        LOG.debug("Found user \"{}\"", username);
        return userMapper.fromEntityToProfileResponse(user.get());
    }

    @Override
    @Transactional
    public UserProfileResponse create(CreateUserRequest dto) {
        LOG.info("Attempting to create user \"{}\"", dto.username());

        Optional<User> checkByUsername = userRepository.findByUsernameIgnoreCase(dto.username());
        if(checkByUsername.isPresent()) {
            String alreadyExists = messageResolver.getMessage("entity.already-exists", "User with username", dto.username());
            LOG.warn(alreadyExists);
            throw new EntityExistsException(alreadyExists);
        }

        Optional<User> checkByEmail = userRepository.findByEmailIgnoreCase(dto.email());
        if(checkByEmail.isPresent()) {
            String alreadyExists = messageResolver.getMessage("entity.already-exists", "User with email", dto.email());
            LOG.warn(alreadyExists);
            throw new EntityExistsException(alreadyExists);
        }

        Optional<Role> userRole = roleRepository.findByNameIgnoreCase(USER_ROLE_NAME);
        if(userRole.isEmpty()) {
            LOG.warn("User creation failed \"{}\" \"{}\" not found", dto.username(), USER_ROLE_NAME);
            throw new EntityNotFoundException(messageResolver.getMessage("entity.not-found", "Role", USER_ROLE_NAME));
        }

        User u = userMapper.fromCreateRequestToEntity(dto);
        u.setRoles(Set.of(userRole.get()));
        u.setPasswordHash(passwordEncoder.encode(u.getPasswordHash()));
        User saved = userRepository.save(u);

        LOG.info("User \"{}\" created", saved.getUsername());
        return userMapper.fromEntityToProfileResponse(saved);
    }
}
