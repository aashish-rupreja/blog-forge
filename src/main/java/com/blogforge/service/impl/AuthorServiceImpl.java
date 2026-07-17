package com.blogforge.service.impl;

import com.blogforge.dto.AuthorProfileResponse;
import com.blogforge.dto.user.UserSummaryResponse;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.UserMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.FollowRepository;
import com.blogforge.repository.UserRepository;
import com.blogforge.service.AuthorService;
import com.blogforge.specification.user.UserSpecification;
import com.blogforge.specification.user.UserSpecificationParams;
import com.blogforge.constants.Constants;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class AuthorServiceImpl implements AuthorService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorServiceImpl.class);

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final UserMapper userMapper;
    private final MessageResolver messageResolver;

    public AuthorServiceImpl(
            UserRepository userRepository,
            FollowRepository followRepository,
            UserMapper userMapper,
            MessageResolver messageResolver) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.userMapper = userMapper;
        this.messageResolver = messageResolver;
    }

    @Override
    public PagedResponse<UserSummaryResponse> getAllAuthorSummary(PaginationRequestParams reqParams, UserSpecificationParams specParams) {
        LOG.trace("Entering getAllAuthorSummary with reqParams: {}, specParams: {}", reqParams, specParams);
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);

        Specification<User> spec = UserSpecification.handleSpecs(specParams);
        spec = spec.and(UserSpecification.hasRole(Constants.AUTHOR_ROLE_NAME));

        LOG.trace("Fetching authors from repository with spec: {}", specParams);
        Page<User> authors = userRepository.findAll(spec, jpaPageable);
        LOG.trace("Fetched {} authors", authors.getNumberOfElements());

        LOG.trace("Mapping Author entities to summary response DTOs");
        PagedResponse<UserSummaryResponse> response = new PagedResponse<>(
                authors.stream().map(userMapper::fromEntityToSummaryResponse).toList(),
                authors.getNumber() + 1,
                authors.getNumberOfElements(),
                authors.getTotalPages(),
                authors.getTotalElements(),
                authors.isEmpty(),
                authors.hasNext()
        );
        LOG.trace("Exiting getAllAuthorSummary with response count: {}", response.getContent().size());
        return response;
    }

    @Override
    public AuthorProfileResponse getAuthorProfile(String username, String authenticatedPrincipalUsername) {
        LOG.trace("Entering getAuthorProfile with username: {}, authenticatedPrincipalUsername: {}", username, authenticatedPrincipalUsername);
        LOG.trace("Finding author by username: {}", username);
        User author = userRepository.findByUsernameAndRoles_Name(username, Constants.AUTHOR_ROLE_NAME)
                .orElseThrow(() -> {
                    String authorNotFound = messageResolver.getMessage("entity.not-found", "Author", username);
                    LOG.warn(authorNotFound);
                    return new EntityNotFoundException(authorNotFound);
                });

        boolean isFollowing = false;
        if (authenticatedPrincipalUsername != null) {
            LOG.trace("Checking if user {} follows {}", authenticatedPrincipalUsername, username);
            isFollowing = followRepository.existsByFollower_UsernameAndFollowing_Username(
                    authenticatedPrincipalUsername,
                    author.getUsername()
            );
        }

        LOG.trace("Mapping Author entity to profile response DTO");
        AuthorProfileResponse response = userMapper.fromEntityToAuthorProfileResponse(author, isFollowing);
        LOG.trace("Exiting getAuthorProfile with response: {}", response);
        return response;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_AUTHOR')")
    public AuthorProfileResponse getMyProfile(String username) {
        LOG.trace("Entering getMyProfile with username: {}", username);
        LOG.trace("Finding author profile by username: {}", username);
        User author = userRepository.findByUsernameAndRoles_Name(username, Constants.AUTHOR_ROLE_NAME)
                .orElseThrow(() -> {
                    String authorNotFound = messageResolver.getMessage("entity.not-found", "Author", username);
                    LOG.warn(authorNotFound);
                    return new EntityNotFoundException(authorNotFound);
                });

        LOG.trace("Mapping own Author entity to profile response DTO");
        AuthorProfileResponse response = userMapper.fromEntityToAuthorProfileResponse(author, false);
        LOG.trace("Exiting getMyProfile with response: {}", response);
        return response;
    }
}
