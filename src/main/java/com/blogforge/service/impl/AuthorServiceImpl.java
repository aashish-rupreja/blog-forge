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
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorServiceImpl implements AuthorService {
    private final String ROLE_AUTHOR = "ROLE_AUTHOR";

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
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);

        Specification<User> spec = UserSpecification.handleSpecs(specParams);
        spec = spec.and(UserSpecification.hasRole(ROLE_AUTHOR));

        Page<User> authors = userRepository.findAll(spec, jpaPageable);
        return new PagedResponse<>(
                authors.stream().map(userMapper::fromEntityToSummaryResponse).toList(),
                authors.getNumber()+1,
                authors.getNumberOfElements(),
                authors.getTotalPages(),
                authors.getTotalElements(),
                authors.isEmpty(),
                authors.hasNext()
        );
    }

    @Override
    public AuthorProfileResponse getAuthorProfile(String username, String authenticatedPrincipalUsername) {
        User author = userRepository.findByUsernameAndRoles_Name(username, ROLE_AUTHOR)
                .orElseThrow(() -> new EntityNotFoundException(
                        messageResolver.getMessage("entity.not-found", "Author", username)
                ));

        boolean isFollowing = false;
        if(authenticatedPrincipalUsername != null) {
                isFollowing = followRepository.existsByFollower_UsernameAndFollowing_Username(
                        authenticatedPrincipalUsername,
                        author.getUsername()
                );
        }

        return userMapper.fromEntityToAuthorProfileResponse(author, isFollowing);
    }
}
