package com.blogforge.service.impl;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.role.CreateRoleRequest;
import com.blogforge.dto.role.RoleResponse;
import com.blogforge.entity.Role;
import com.blogforge.exception.MessageResolver;
import com.blogforge.mapper.RoleMapper;
import com.blogforge.pagination.PagedRequest;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.repository.RoleRepository;
import com.blogforge.service.RoleService;
import com.blogforge.specification.role.RoleSpecification;
import com.blogforge.specification.role.RoleSpecificationParams;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class RoleServiceImpl implements RoleService {

    private final Logger LOG = LoggerFactory.getLogger(RoleServiceImpl.class);

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final MessageResolver messageResolver;

    public RoleServiceImpl(RoleRepository roleRepository, RoleMapper roleMapper, MessageResolver messageResolver) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.messageResolver = messageResolver;
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

    @Override
    public RoleResponse getByName(String name) {
        LOG.debug("Attempting to find Role \"{}\"", name);
        Optional<Role> r = roleRepository.findByNameIgnoreCase(name);
        if(r.isEmpty()) {
            String notFoundMessage = messageResolver.getMessage("entity.not-found", "Role", name);
            LOG.debug(notFoundMessage);
            throw new EntityNotFoundException(notFoundMessage);
        }
        LOG.debug("Role \"{}\" found", name);
        return roleMapper.fromEntityToResponse(r.get());
    }

    @Override
    public RoleResponse create(CreateRoleRequest dto) {
        LOG.debug("Attempting to create Role \"{}\"", dto.name());

        String roleName = dto.name().toUpperCase();
        if(!roleName.startsWith("ROLE")) roleName = "ROLE_"+roleName;
        CreateRoleRequest normalized = new CreateRoleRequest(roleName);

        Optional<Role> r = roleRepository.findByNameIgnoreCase(normalized.name());
        if(r.isPresent()) {
            String alreadyExistsMessage = messageResolver.getMessage("entity.already-exists", "Role", roleName);
            LOG.debug(alreadyExistsMessage);
            throw new EntityExistsException(alreadyExistsMessage);
        }

        Role saved = roleRepository.save(roleMapper.fromCreateRequestToEntity(normalized));
        LOG.debug("Role \"{}\" created", roleName);
        return roleMapper.fromEntityToResponse(saved);
    }

    @Override
    public GenericResponse deleteOne(String roleName) {
        LOG.debug("Attempting to delete Role \"{}\"", roleName);
        Optional<Role> r = roleRepository.findByNameIgnoreCase(roleName);
        if(r.isEmpty()) {
            String notExistsMessage = messageResolver.getMessage("entity.not-found", "Role", roleName);
            LOG.debug(notExistsMessage);
            throw new EntityNotFoundException(notExistsMessage);
        }
        roleRepository.delete(r.get());
        String deleted = "Role \"##\" deleted".replaceAll("##", roleName);
        LOG.debug(deleted);
        return new GenericResponse(deleted);
    }
}
