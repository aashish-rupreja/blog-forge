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
import com.blogforge.service.UserService;
import com.blogforge.specification.user.UserSpecification;
import com.blogforge.specification.user.UserSpecificationParams;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final MessageResolver messageResolver;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, MessageResolver messageResolver) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.messageResolver = messageResolver;
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
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()) {
            String notExistsMessage = messageResolver.getMessage("entity.not-found", "User", username);
            LOG.debug(notExistsMessage);
            throw new EntityNotFoundException(notExistsMessage);
        }
        LOG.debug("Found user \"{}\"", username);
        return userMapper.fromEntityToProfileResponse(user.get());
    }
}
