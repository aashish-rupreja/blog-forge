package com.blogforge.mapper;

import com.blogforge.dto.BaseResponse;
import com.blogforge.dto.role.*;
import com.blogforge.entity.Role;
import com.blogforge.entity.User;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    public Role fromCreateRequestToEntity(CreateRoleRequest dto) {
        Role r = new Role();
        r.setName(dto.name());
        r.setRoleType(dto.roleType());
        return r;
    }

    public RoleResponse fromEntityToResponse(Role r) {
        Set<String> holders = (r.getHolders() != null)
                ? r.getHolders().stream().map(User::getUsername).collect(Collectors.toSet())
                : new HashSet<>();

        return new RoleResponse(
                new BaseResponse(
                        r.getUuid(),
                        r.getCreatedAt(),
                        r.getUpdatedAt()),
                r.getName(),
                r.getRoleType(),
                holders,
                holders.size()
        );
    }
}
