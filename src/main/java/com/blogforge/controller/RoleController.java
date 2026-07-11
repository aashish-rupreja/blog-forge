package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.role.CreateRoleRequest;
import com.blogforge.dto.role.RoleResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.RoleService;
import com.blogforge.specification.role.RoleSpecificationParams;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(path = "/api/v1/roles/{name}")
    public ResponseEntity<RoleResponse> getByName(@PathVariable String name) {
        RoleResponse rr = roleService.getByName(name);
        return new ResponseEntity<>(rr, HttpStatus.OK);
    }

    @PostMapping(path = "/api/v1/roles")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody CreateRoleRequest dto) {
        RoleResponse rr = roleService.create(dto);
        return new ResponseEntity<>(rr, HttpStatus.CREATED);
    }

    @DeleteMapping(path = "/api/v1/roles/{name}")
    public ResponseEntity<GenericResponse> deleteOne(@PathVariable String name) {
        GenericResponse gr = roleService.deleteOne(name);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }
}
