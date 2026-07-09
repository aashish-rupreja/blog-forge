package com.blogforge.controller;

import com.blogforge.dto.role.RoleResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.RoleService;
import com.blogforge.specification.role.RoleSpecificationParams;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping(path = "/api/v1/roles")
    public ResponseEntity<PagedResponse<RoleResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute RoleSpecificationParams specParams
            ) {
        PagedResponse<RoleResponse> roles = roleService.getAll(reqParams, specParams);
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }
}
