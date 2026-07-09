package com.blogforge.service.impl;

import com.blogforge.dto.role.RoleResponse;
import com.blogforge.entity.Role;
import com.blogforge.mapper.RoleMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.RoleRepository;
import com.blogforge.service.RoleService;
import com.blogforge.specification.role.RoleSpecification;
import com.blogforge.specification.role.RoleSpecificationParams;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    public PagedResponse<RoleResponse> getAll(PaginationRequestParams reqParams, RoleSpecificationParams specParams) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<Role> roleSpecs = RoleSpecification.handleSpecs(specParams);
        Page<Role> roles = roleRepository.findAll(roleSpecs, jpaPageable);
        return new PagedResponse<>(
                roles.stream().map(roleMapper::fromEntityToResponse).toList(),
                roles.getNumber()+1,
                roles.getNumberOfElements(),
                roles.getTotalPages(),
                roles.getTotalElements(),
                roles.isEmpty(),
                roles.hasNext()
        );
    }
}
