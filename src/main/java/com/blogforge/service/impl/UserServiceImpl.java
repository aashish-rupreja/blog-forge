package com.blogforge.service.impl;

import com.blogforge.dto.user.UserSummaryResponse;
import com.blogforge.entity.User;
import com.blogforge.mapper.UserMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.UserRepository;
import com.blogforge.service.UserService;
import com.blogforge.specification.user.UserSpecification;
import com.blogforge.specification.user.UserSpecificationParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
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
}
