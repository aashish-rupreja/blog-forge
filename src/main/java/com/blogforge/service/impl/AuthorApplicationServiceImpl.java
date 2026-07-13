package com.blogforge.service.impl;

import com.blogforge.dto.authorapplication.AuthorApplicationResponse;
import com.blogforge.dto.authorapplication.MyAuthorApplicationsRequest;
import com.blogforge.entity.AuthorApplication;
import com.blogforge.mapper.AuthorApplicationMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.AuthorApplicationRepository;
import com.blogforge.service.AuthorApplicationService;
import com.blogforge.specification.authorapplication.AuthorApplicationSpecification;
import com.blogforge.specification.authorapplication.AuthorApplicationSpecificationParams;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorApplicationServiceImpl implements AuthorApplicationService {

    private final AuthorApplicationRepository authorApplicationRepository;
    private final AuthorApplicationMapper authorApplicationMapper;

    public AuthorApplicationServiceImpl(AuthorApplicationRepository authorApplicationRepository, AuthorApplicationMapper authorApplicationMapper) {
        this.authorApplicationRepository = authorApplicationRepository;
        this.authorApplicationMapper = authorApplicationMapper;
    }

    @Override
    public PagedResponse<AuthorApplicationResponse> getAll(PaginationRequestParams reqParams, AuthorApplicationSpecificationParams specParams) {
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<AuthorApplication> spec = AuthorApplicationSpecification.handleSpecs(specParams);

        Page<AuthorApplication> authorApplications = authorApplicationRepository.findAll(spec, jpaPageable);
        return new PagedResponse<>(
                authorApplications.stream().map(authorApplicationMapper::fromEntityToResponse).toList(),
                authorApplications.getNumber()+1,
                authorApplications.getSize(),
                authorApplications.getTotalPages(),
                authorApplications.getNumberOfElements(),
                authorApplications.isEmpty(),
                authorApplications.hasNext()
        );
    }

    @Override
    public PagedResponse<AuthorApplicationResponse> getMyAuthorApplications(PaginationRequestParams reqParams, MyAuthorApplicationsRequest specParams) {

        String currentAuthenticatedUser = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);

        Specification<AuthorApplication> spec = AuthorApplicationSpecification.handleMyApplicationSpecs(specParams, currentAuthenticatedUser);

        Page<AuthorApplication> myAuthorApplications = authorApplicationRepository.findAll(spec, jpaPageable);

        return new PagedResponse<>(
                myAuthorApplications.stream().map(authorApplicationMapper::fromEntityToResponse).toList(),
                myAuthorApplications.getNumber()+1,
                myAuthorApplications.getSize(),
                myAuthorApplications.getTotalPages(),
                myAuthorApplications.getTotalElements(),
                myAuthorApplications.isEmpty(),
                myAuthorApplications.hasNext()
        );
    }
}
