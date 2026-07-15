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
    public PagedResponse<RoleResponse> getAll(PaginationRequestParams paginationRequestParams,
                                              RoleSpecificationParams roleSpecificationParams) {

        PagedRequest defaultsIfAnyInvalid = PagedRequest.initWithDefaultsIfAnyInvalid(paginationRequestParams);
        Pageable pageable = PagedRequest.getJPAPageRequest(defaultsIfAnyInvalid);
        Specification<Role> roleSpecifications = RoleSpecification.handleSpecs(roleSpecificationParams);
        Page<Role> roles = roleRepository.findAll(roleSpecifications, pageable);

        LOG.debug("Fetching all roles");
        return new PagedResponse<>(
                roles.stream().map(roleMapper::fromEntityToResponse).toList(),
                roles.getNumber() + 1,
                roles.getNumberOfElements(),
                roles.getTotalPages(),
                roles.getTotalElements(),
                roles.isEmpty(),
                roles.hasNext()
        );
    }

    @Override
    public RoleResponse getByName(String name) {
        Role role = findRoleOrThrow(name);
        return roleMapper.fromEntityToResponse(role);
    }

    // TODO: Add authorization so only admin can create role
    @Override
    @Transactional
    public RoleResponse create(CreateRoleRequest dto) {
        LOG.info("Attempting to create Role \"{}\"", dto.name());

        // normalized form makes the role name uppercase and adds prefix ROLE_
        // e.g. author -> ROLE_AUTHOR
        String normalizeRoleName = normalizeRoleName(dto.name());

        boolean roleExists = roleRepository.existsByNameIgnoreCase(normalizeRoleName);
        if (roleExists) {
            String roleDoesExist = messageResolver.getMessage("entity.already-exists", "Role", normalizeRoleName);
            LOG.warn(roleDoesExist);
            throw new EntityExistsException(roleDoesExist);
        }

        CreateRoleRequest normalized = new CreateRoleRequest(normalizeRoleName, dto.roleType());
        Role role = roleMapper.fromCreateRequestToEntity(normalized);
        role.setRoleType(RoleType.CUSTOM);
        Role saved = roleRepository.save(role);

        LOG.info("Role \"{}\" created", saved.getName());
        return roleMapper.fromEntityToResponse(saved);
    }

    @Override
    @Transactional
    public GenericResponse deleteOne(String name) {
        LOG.info("Attempting to delete Role \"{}\"", name);

        Role role = findRoleOrThrow(name);
        if(role.getRoleType() == RoleType.SYSTEM) {
            String deleteNotAllowedMsg = messageResolver.getMessage("role.system.delete.not-allowed", name);
            throw new IllegalArgumentException(deleteNotAllowedMsg);
        }
        roleRepository.delete(role);

        String deleteMsg = messageResolver.getMessage("entity.delete.success", "1", "Role");
        LOG.info(deleteMsg);
        return new GenericResponse(deleteMsg);
    }

    @Override
    @Transactional
    public GenericResponse deleteAllIn(DeleteRoleRequest deleteRoleRequest) {
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
        long deletedCount = roleRepository.deleteAllIn(normalized);

        String deleteMsg = messageResolver.getMessage("entity.delete.success", String.valueOf(deletedCount), "Roles");
        LOG.info(deleteMsg);
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
