package com.blogforge.service.impl;

import com.blogforge.constants.Constants;
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
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthorApplicationServiceImpl implements AuthorApplicationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthorApplicationServiceImpl.class);

    private final AuthorApplicationRepository authorApplicationRepository;
    private final UserRepository userRepository;
    private final AuthorApplicationMapper authorApplicationMapper;
    private final UserService userService;
    private final MessageResolver messageResolver;

    public AuthorApplicationServiceImpl(
            AuthorApplicationRepository authorApplicationRepository,
            UserRepository userRepository,
            UserService userService,
            AuthorApplicationMapper authorApplicationMapper,
            MessageResolver messageResolver) {
        this.authorApplicationRepository = authorApplicationRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.authorApplicationMapper = authorApplicationMapper;
        this.messageResolver = messageResolver;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PagedResponse<AuthorApplicationResponse> getAll(PaginationRequestParams reqParams, AuthorApplicationSpecificationParams specParams) {
        LOG.trace("Entering getAll with reqParams: {}, specParams: {}", reqParams, specParams);
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);
        Specification<AuthorApplication> spec = AuthorApplicationSpecification.handleSpecs(specParams);

        LOG.trace("Fetching author applications from repository with spec: {}", specParams);
        Page<AuthorApplication> authorApplications = authorApplicationRepository.findAll(spec, jpaPageable);
        LOG.trace("Fetched {} author applications", authorApplications.getNumberOfElements());

        LOG.trace("Mapping AuthorApplication entities to response DTOs");
        PagedResponse<AuthorApplicationResponse> response = new PagedResponse<>(
                authorApplications.stream().map(authorApplicationMapper::fromEntityToResponse).toList(),
                authorApplications.getNumber() + 1,
                authorApplications.getSize(),
                authorApplications.getTotalPages(),
                authorApplications.getNumberOfElements(),
                authorApplications.isEmpty(),
                authorApplications.hasNext()
        );
        LOG.trace("Exiting getAll with response count: {}", response.getContent().size());
        return response;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public PagedResponse<AuthorApplicationResponse> getMyAuthorApplications(
            PaginationRequestParams reqParams,
            MyAuthorApplicationsRequest specParams,
            String currentAuthenticatedUsername) {
        LOG.trace("Entering getMyAuthorApplications with reqParams: {}, specParams: {}, currentAuthenticatedUsername: {}", reqParams, specParams, currentAuthenticatedUsername);
        PagedRequest pr = PagedRequest.initWithDefaultsIfAnyInvalid(reqParams);
        Pageable jpaPageable = PagedRequest.getJPAPageRequest(pr);

        Specification<AuthorApplication> spec = AuthorApplicationSpecification.handleMyApplicationSpecs(specParams, currentAuthenticatedUsername);

        LOG.trace("Fetching own author applications from repository for user: {}", currentAuthenticatedUsername);
        Page<AuthorApplication> myAuthorApplications = authorApplicationRepository.findAll(spec, jpaPageable);
        LOG.trace("Fetched {} own author applications", myAuthorApplications.getNumberOfElements());

        LOG.trace("Mapping own AuthorApplication entities to response DTOs");
        PagedResponse<AuthorApplicationResponse> response = new PagedResponse<>(
                myAuthorApplications.stream().map(authorApplicationMapper::fromEntityToResponse).toList(),
                myAuthorApplications.getNumber() + 1,
                myAuthorApplications.getSize(),
                myAuthorApplications.getTotalPages(),
                myAuthorApplications.getTotalElements(),
                myAuthorApplications.isEmpty(),
                myAuthorApplications.hasNext()
        );
        LOG.trace("Exiting getMyAuthorApplications with response count: {}", response.getContent().size());
        return response;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public AuthorApplicationResponse create(CreateAuthorApplicationRequest dto, String currentAuthenticatedUsername) {
        LOG.trace("Entering create with dto: {}, currentAuthenticatedUsername: {}", dto, currentAuthenticatedUsername);

        LOG.trace("Finding existing application by applicant username: {}", currentAuthenticatedUsername);
        Optional<AuthorApplication> check =
                authorApplicationRepository.findByApplicant_Username(currentAuthenticatedUsername);

        if (check.isPresent()) {
            AuthorApplicationStatus existingApplicationStatus = check.get().getStatus();
            if (existingApplicationStatus == AuthorApplicationStatus.PENDING) {
                String alreadyPending = messageResolver.getMessage("author-application.already-pending");
                LOG.warn(alreadyPending);
                throw new AuthorApplicationAlreadyExistsException(alreadyPending);
            }
        }

        LOG.trace("Mapping create request DTO to AuthorApplication entity");
        AuthorApplication newApplication = authorApplicationMapper.fromCreateRequestToEntity(dto);
        
        LOG.trace("Finding applicant user entity by username: {}", currentAuthenticatedUsername);
        User applicant = userRepository.findByUsernameIgnoreCase(currentAuthenticatedUsername)
                .orElseThrow(() -> {
                    String applicationNotFound = messageResolver.getMessage(
                            "entity.not-found",
                            "User", currentAuthenticatedUsername);
                    LOG.warn(applicationNotFound);
                    return new EntityNotFoundException(applicationNotFound);
                });
        newApplication.setApplicant(applicant);

        LOG.trace("Finding reviewer user entity by username: {}", Constants.DEFAULT_ADMIN);
        User reviewer = userRepository.findByUsernameIgnoreCase(Constants.DEFAULT_ADMIN)
                .orElseThrow(() -> {
                    String applicationNotFound = messageResolver.getMessage(
                            "entity.not-found",
                            "User", Constants.DEFAULT_ADMIN);
                    LOG.warn(applicationNotFound);
                    return new EntityNotFoundException(applicationNotFound);
                });
        newApplication.setApplicationReviewer(reviewer);

        LOG.trace("Saving new author application to repository");
        AuthorApplication saved = authorApplicationRepository.save(newApplication);
        LOG.info("New application created for {}", saved.getApplicant().getUsername());
        
        LOG.trace("Mapping saved AuthorApplication entity to response DTO");
        AuthorApplicationResponse response = authorApplicationMapper.fromEntityToResponse(saved);
        LOG.trace("Exiting create with response: {}", response);
        return response;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public AuthorApplicationResponse getSingleApplication(UUID id) {
        LOG.trace("Entering getSingleApplication with id: {}", id);
        LOG.trace("Finding author application by id: {}", id);
        AuthorApplication aa = authorApplicationRepository.findById(id)
                .orElseThrow(() -> {
                    String applicationNotFound = messageResolver.getMessage(
                            "entity.not-found",
                            "Author Application", id.toString());
                    LOG.warn(applicationNotFound);
                    return new EntityNotFoundException(applicationNotFound);
                });

        LOG.trace("Mapping AuthorApplication entity to response DTO");
        AuthorApplicationResponse response = authorApplicationMapper.fromEntityToResponse(aa);
        LOG.trace("Exiting getSingleApplication with response: {}", response);
        return response;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public AuthorApplicationResponse approveApplication(UUID id, UpdateAuthorApplicationRequest dto) {
        LOG.trace("Entering approveApplication with id: {}, dto: {}", id, dto);
        LOG.trace("Finding author application for approval by id: {}", id);
        AuthorApplication aa = authorApplicationRepository.findById(id)
                .orElseThrow(() -> {
                    String applicationNotFound = messageResolver.getMessage(
                            "entity.not-found",
                            "Author Application", id.toString());
                    LOG.warn(applicationNotFound);
                    return new EntityNotFoundException(applicationNotFound);
                });

        if (!aa.getStatus().canTransitionTo(AuthorApplicationStatus.APPROVED.toString())) {
            String illegalTransition = messageResolver.getMessage("author.application.illegal-transition", aa.getStatus().toString(), AuthorApplicationStatus.APPROVED.toString());
            LOG.warn(illegalTransition);
            throw new AuthorApplicationTransitionException(illegalTransition);
        }

        aa.setStatus(AuthorApplicationStatus.APPROVED);
        aa.setReviewerRemarks(dto.reviewerRemarks());
        
        LOG.trace("Assigning author role to applicant user uuid: {}", aa.getApplicant().getUuid());
        userService.assignAuthorRole(aa.getApplicant().getUuid());
        
        LOG.trace("Saving updated author application to repository");
        AuthorApplication saved = authorApplicationRepository.save(aa);
        LOG.info("Author application {} for {}", AuthorApplicationStatus.APPROVED.toString(), aa.getApplicant().getUsername());
        
        LOG.trace("Mapping approved AuthorApplication entity to response DTO");
        AuthorApplicationResponse response = authorApplicationMapper.fromEntityToResponse(saved);
        LOG.trace("Exiting approveApplication with response: {}", response);
        return response;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public AuthorApplicationResponse rejectApplication(UUID id, UpdateAuthorApplicationRequest dto) {
        LOG.trace("Entering rejectApplication with id: {}, dto: {}", id, dto);
        LOG.trace("Finding author application for rejection by id: {}", id);
        AuthorApplication aa = authorApplicationRepository.findById(id)
                .orElseThrow(() -> {
                    String applicationNotFound = messageResolver.getMessage(
                            "entity.not-found",
                            "Author Application", id.toString());
                    LOG.warn(applicationNotFound);
                    return new EntityNotFoundException(applicationNotFound);
                });

        if (!aa.getStatus().canTransitionTo(AuthorApplicationStatus.REJECTED.toString())) {
            String illegalTransition = messageResolver.getMessage("author.application.illegal-transition", aa.getStatus().toString(), AuthorApplicationStatus.REJECTED.toString());
            LOG.warn(illegalTransition);
            throw new AuthorApplicationTransitionException(illegalTransition);
        }

        aa.setStatus(AuthorApplicationStatus.REJECTED);
        aa.setReviewerRemarks(dto.reviewerRemarks());
        
        LOG.trace("Saving rejected author application to repository");
        AuthorApplication saved = authorApplicationRepository.save(aa);
        LOG.info("Author application {} for {}", AuthorApplicationStatus.REJECTED.toString(), aa.getApplicant().getUsername());
        
        LOG.trace("Mapping rejected AuthorApplication entity to response DTO");
        AuthorApplicationResponse response = authorApplicationMapper.fromEntityToResponse(saved);
        LOG.trace("Exiting rejectApplication with response: {}", response);
        return response;
    }


}