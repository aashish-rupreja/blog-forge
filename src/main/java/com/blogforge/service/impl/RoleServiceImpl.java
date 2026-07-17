package com.blogforge.service.impl;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.role.CreateRoleRequest;
import com.blogforge.dto.role.DeleteRoleRequest;
import com.blogforge.dto.role.RoleResponse;
import com.blogforge.entity.Role;
import com.blogforge.entity.RoleType;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.RoleMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.RoleRepository;
import com.blogforge.service.RoleService;
import com.blogforge.specification.role.RoleSpecification;
import com.blogforge.specification.role.RoleSpecificationParams;
import com.blogforge.constants.Constants;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class RoleServiceImpl implements RoleService {

    private static final Logger LOG = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final MessageResolver messageResolver;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper, MessageResolver messageResolver) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.messageResolver = messageResolver;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public PagedResponse<RoleResponse> getAll(PaginationRequestParams paginationRequestParams,
                                              RoleSpecificationParams roleSpecificationParams) {
        LOG.trace("Entering getAll with paginationRequestParams: {}, roleSpecificationParams: {}", paginationRequestParams, roleSpecificationParams);
        PagedRequest defaultsIfAnyInvalid = PagedRequest.initWithDefaultsIfAnyInvalid(paginationRequestParams);
        Pageable pageable = PagedRequest.getJPAPageRequest(defaultsIfAnyInvalid);
        Specification<Role> roleSpecifications = RoleSpecification.handleSpecs(roleSpecificationParams);
        
        LOG.trace("Fetching roles from repository with specifications");
        Page<Role> roles = roleRepository.findAll(roleSpecifications, pageable);
        LOG.trace("Fetched {} roles", roles.getNumberOfElements());

        LOG.debug("Fetching all roles");
        LOG.trace("Mapping Role entities to response DTOs");
        PagedResponse<RoleResponse> response = new PagedResponse<>(
                roles.stream().map(roleMapper::fromEntityToResponse).toList(),
                roles.getNumber() + 1,
                roles.getNumberOfElements(),
                roles.getTotalPages(),
                roles.getTotalElements(),
                roles.isEmpty(),
                roles.hasNext()
        );
        LOG.trace("Exiting getAll with response count: {}", response.getContent().size());
        return response;
    }

    @Override
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public RoleResponse getByName(String name) {
        LOG.trace("Entering getByName with name: {}", name);
        Role role = findRoleOrThrow(name);
        
        LOG.trace("Mapping Role entity to response DTO");
        RoleResponse response = roleMapper.fromEntityToResponse(role);
        LOG.trace("Exiting getByName with response: {}", response);
        return response;
    }


    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public RoleResponse create(CreateRoleRequest dto) {
        LOG.trace("Entering create with dto: {}", dto);
        LOG.info("Attempting to create Role \"{}\"", dto.name());

        // normalized form makes the role name uppercase and adds prefix ROLE_
        // e.g. author -> ROLE_AUTHOR
        String normalizeRoleName = normalizeRoleName(dto.name());

        LOG.trace("Checking if role already exists in repository with name: {}", normalizeRoleName);
        boolean roleExists = roleRepository.existsByNameIgnoreCase(normalizeRoleName);
        if (roleExists) {
            String roleDoesExist = messageResolver.getMessage("entity.already-exists", "Role", normalizeRoleName);
            LOG.warn(roleDoesExist);
            throw new EntityExistsException(roleDoesExist);
        }

        CreateRoleRequest normalized = new CreateRoleRequest(normalizeRoleName, dto.roleType());
        LOG.trace("Mapping normalized CreateRoleRequest DTO to Role entity");
        Role role = roleMapper.fromCreateRequestToEntity(normalized);
        role.setRoleType(RoleType.CUSTOM);
        
        LOG.trace("Saving new role to repository");
        Role saved = roleRepository.save(role);

        LOG.info("Role \"{}\" created", saved.getName());
        
        LOG.trace("Mapping saved Role entity to response DTO");
        RoleResponse response = roleMapper.fromEntityToResponse(saved);
        LOG.trace("Exiting create with response: {}", response);
        return response;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GenericResponse deleteOne(String name) {
        LOG.trace("Entering deleteOne with name: {}", name);
        LOG.info("Attempting to delete Role \"{}\"", name);

        Role role = findRoleOrThrow(name);
        if(role.getRoleType() == RoleType.SYSTEM) {
            String deleteNotAllowedMsg = messageResolver.getMessage("role.system.delete.not-allowed", name);
            throw new IllegalArgumentException(deleteNotAllowedMsg);
        }
        
        LOG.trace("Deleting role from repository: {}", role.getName());
        roleRepository.delete(role);

        String deleteMsg = messageResolver.getMessage("entity.delete.success", "1", "Role");
        LOG.info(deleteMsg);
        LOG.trace("Exiting deleteOne with message: {}", deleteMsg);
        return new GenericResponse(deleteMsg);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public GenericResponse deleteAllIn(DeleteRoleRequest deleteRoleRequest) {
        LOG.trace("Entering deleteAllIn with deleteRoleRequest: {}", deleteRoleRequest);
        Set<String> normalized = new HashSet<>();
        for (String name : deleteRoleRequest.roles()) {
            boolean isSystemRole = name.equals(Constants.ADMIN_ROLE_NAME)
                    || name.equals(Constants.AUTHOR_ROLE_NAME)
                    || name.equals(Constants.USER_ROLE_NAME);

            if (isSystemRole) {
                String deleteNotAllowedMsg = messageResolver.getMessage("role.system.delete.not-allowed", name);
                throw new IllegalArgumentException(deleteNotAllowedMsg);
            }
            normalized.add(normalizeRoleName(name));
        }

        if (normalized.isEmpty()) {
            String entityToDeleteNotProvided = messageResolver.getMessage("entity.to-delete.not-provided", "Role(s)");
            LOG.debug(entityToDeleteNotProvided);
            throw new IllegalArgumentException(entityToDeleteNotProvided);
        }

        LOG.debug("Attempting to delete Roles {}", normalized.toString());
        
        LOG.trace("Deleting multiple roles from repository: {}", normalized);
        long deletedCount = roleRepository.deleteAllIn(normalized);

        String deleteMsg = messageResolver.getMessage("entity.delete.success", String.valueOf(deletedCount), "Roles");
        LOG.info(deleteMsg);
        LOG.trace("Exiting deleteAllIn with message: {}", deleteMsg);
        return new GenericResponse(deleteMsg);
    }

    private Role findRoleOrThrow(String name) {
        LOG.debug("Attempting to find Role \"{}\"", name);
        Role role = roleRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> {
                    String notFoundMessage = messageResolver.getMessage("entity.not-found", "Role", name);
                    LOG.debug(notFoundMessage);
                    return new EntityNotFoundException(notFoundMessage);
                });
        return role;
    }

    private String normalizeRoleName(String name) {
        name = name.toUpperCase();
        name = (!name.startsWith("ROLE_")) ? name : "ROLE_" + name;
        return name;
    }
}
