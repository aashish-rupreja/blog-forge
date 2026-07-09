package com.blogforge.service.impl;

import com.blogforge.dto.user.UserSummaryResponse;
import com.blogforge.entity.User;
import com.blogforge.mapper.UserMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.UserRepository;
import com.blogforge.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public PagedResponse<UserSummaryResponse> getAllUserSummary(PaginationRequestParams reqParams) {
        PagedRequest pr = new PagedRequest(
                reqParams.pageNo()-1,
                reqParams.pageSize(),
                reqParams.sortDirection(),
                reqParams.sortBy()
        );
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Page<User> users = userRepository.findAll(jpaPageable);
        return new PagedResponse<>(
                users.getContent().stream().map(userMapper::fromEntityToSummaryResponse).toList(),
                users.getNumber()+1,
                users.getNumberOfElements(),
                users.getTotalPages(),
                users.isEmpty(),
                users.hasNext()
        );
    }
}
