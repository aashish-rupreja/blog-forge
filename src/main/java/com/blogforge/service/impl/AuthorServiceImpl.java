package com.blogforge.service.impl;

import com.blogforge.dto.user.UserProfileResponse;
import com.blogforge.dto.user.UserSummaryResponse;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.UserMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.UserRepository;
import com.blogforge.service.AuthorService;
import com.blogforge.specification.user.UserSpecification;
import com.blogforge.specification.user.UserSpecificationParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthorServiceImpl implements AuthorService {
    private final String ROLE_AUTHOR = "ROLE_AUTHOR";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MessageResolver messageResolver;

    public AuthorServiceImpl(UserRepository userRepository, UserMapper userMapper, MessageResolver messageResolver) {
        this.userRepository = userRepository;
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
    public UserProfileResponse getAuthorProfile(String username) {
        User author = userRepository.findByUsernameAndRoles_Name(username, ROLE_AUTHOR)
                .orElseThrow(() -> new UsernameNotFoundException(
                        messageResolver.getMessage("user.username.not-found", username)
                ));
        return userMapper.fromEntityToProfileResponse(author);
    }
}
