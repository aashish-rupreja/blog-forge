package com.blogforge.service.impl;

import com.blogforge.dto.authorapplication.AuthorApplicationResponse;
import com.blogforge.dto.authorapplication.CreateAuthorApplicationRequest;
import com.blogforge.dto.authorapplication.MyAuthorApplicationsRequest;
import com.blogforge.dto.authorapplication.UpdateAuthorApplicationRequest;
import com.blogforge.entity.AuthorApplication;
import com.blogforge.entity.AuthorApplicationStatus;
import com.blogforge.entity.User;
import com.blogforge.exception.AuthorApplicationAlreadyExistsException;
import com.blogforge.exception.AuthorApplicationTransitionException;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.AuthorApplicationMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.AuthorApplicationRepository;
import com.blogforge.repository.UserRepository;
import com.blogforge.service.AuthorApplicationService;
import com.blogforge.service.UserService;
import com.blogforge.specification.authorapplication.AuthorApplicationSpecification;
import com.blogforge.specification.authorapplication.AuthorApplicationSpecificationParams;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthorApplicationServiceImpl implements AuthorApplicationService {

    private final AuthorApplicationRepository authorApplicationRepository;
    private final AuthorApplicationMapper authorApplicationMapper;
    private final UserService userService;

    private final MessageResolver messageResolver;

    public AuthorApplicationServiceImpl(
            AuthorApplicationRepository authorApplicationRepository,
            UserService userService,
            AuthorApplicationMapper authorApplicationMapper,
            MessageResolver messageResolver) {
        this.authorApplicationRepository = authorApplicationRepository;
        this.userService = userService;
        this.authorApplicationMapper = authorApplicationMapper;
        this.messageResolver = messageResolver;
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

        String currentAuthenticatedUser = "groot.tree";

//        String currentAuthenticatedUser = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getName();

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

    @Override
    public AuthorApplicationResponse create(CreateAuthorApplicationRequest dto) {
        String currentAuthenticatedUser = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        boolean isAuthor = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_AUTHOR"));

        if(isAuthor) {
            String alreadyAuthor = messageResolver.getMessage("authorApplication.already-exists.approved");
            throw new IllegalStateException(alreadyAuthor);
        }

        Optional<AuthorApplication> check =
                authorApplicationRepository.findByApplicant_Username(currentAuthenticatedUser);

        if(check.isPresent()) {
            AuthorApplicationStatus existingApplicationStatus = check.get().getStatus();
            if(existingApplicationStatus == AuthorApplicationStatus.PENDING) {
                String alreadyPending = messageResolver.getMessage("authorApplication.already-exists.pending");
                throw new AuthorApplicationAlreadyExistsException(alreadyPending);
            }
        }

        AuthorApplication newApplication = authorApplicationMapper.fromCreateRequestToEntity(dto);
        AuthorApplication saved = authorApplicationRepository.save(newApplication);
        return authorApplicationMapper.fromEntityToResponse(saved);
    }

    @Override
    public AuthorApplicationResponse getSingleApplication(UUID id) {
        AuthorApplication aa = authorApplicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Author Application", id.toString()
                )));

        return authorApplicationMapper.fromEntityToResponse(aa);
    }

    @Override
    public AuthorApplicationResponse approveApplication(UUID id, UpdateAuthorApplicationRequest dto) {
        AuthorApplication aa = authorApplicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Author Application", id.toString()
                )));

        if(!aa.getStatus().canTransitionTo(AuthorApplicationStatus.APPROVED.toString())) {
            String illegalTransition = messageResolver.getMessage("author.application.illegal-transition", aa.getStatus().toString(), AuthorApplicationStatus.APPROVED.toString());
            throw new AuthorApplicationTransitionException(illegalTransition);
        }

        aa.setStatus(AuthorApplicationStatus.APPROVED);
        aa.setReviewerRemarks(dto.reviewerRemarks());
        userService.assignAuthorRole(aa.getApplicant().getUuid());
        AuthorApplication saved = authorApplicationRepository.save(aa);
        return authorApplicationMapper.fromEntityToResponse(saved);
    }

    @Override
    public AuthorApplicationResponse rejectApplication(UUID id, UpdateAuthorApplicationRequest dto) {
        AuthorApplication aa = authorApplicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(messageResolver.getMessage(
                        "entity.not-found",
                        "Author Application", id.toString()
                )));

        if(!aa.getStatus().canTransitionTo(AuthorApplicationStatus.REJECTED.toString())) {
            String illegalTransition = messageResolver.getMessage("author.application.illegal-transition", aa.getStatus().toString(), AuthorApplicationStatus.REJECTED.toString());
            throw new AuthorApplicationTransitionException(illegalTransition);
        }

        aa.setStatus(AuthorApplicationStatus.REJECTED);
        aa.setReviewerRemarks(dto.reviewerRemarks());
        AuthorApplication saved = authorApplicationRepository.save(aa);
        return authorApplicationMapper.fromEntityToResponse(saved);
    }


}