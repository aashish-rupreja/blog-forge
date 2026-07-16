package com.blogforge.controller;

import com.blogforge.dto.GenericResponse;
import com.blogforge.dto.role.CreateRoleRequest;
import com.blogforge.dto.role.DeleteRoleRequest;
import com.blogforge.dto.role.RoleResponse;
import com.blogforge.pagination.PagedResponse;
import com.blogforge.pagination.PaginationRequestParams;
import com.blogforge.service.RoleService;
import com.blogforge.specification.role.RoleSpecificationParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Roles", description = "Endpoints for managing user roles (Admin only)")
@RestController
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Get all roles", description = "Returns a paginated list of roles. Supports filtering. Requires Admin privileges.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of roles returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content)
    })
    @GetMapping(path = "/api/v1/roles")
    public ResponseEntity<PagedResponse<RoleResponse>> getAll(
            @ModelAttribute PaginationRequestParams reqParams,
            @ModelAttribute RoleSpecificationParams specParams
            ) {
        PagedResponse<RoleResponse> roles = roleService.getAll(reqParams, specParams);
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @Operation(summary = "Get role by name", description = "Fetches details of a specific role by its name. Requires Admin privileges.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role returned successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content)
    })
    @GetMapping(path = "/api/v1/roles/{name}")
    public ResponseEntity<RoleResponse> getByName(
            @Parameter(description = "The unique name of the role") @PathVariable String name) {
        RoleResponse rr = roleService.getByName(name);
        return new ResponseEntity<>(rr, HttpStatus.OK);
    }

    @Operation(summary = "Create a role", description = "Creates a new role. Requires Admin privileges.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content),
            @ApiResponse(responseCode = "409", description = "Role already exists", content = @Content)
    })
    @PostMapping(path = "/api/v1/roles")
    public ResponseEntity<RoleResponse> create(@Valid @RequestBody CreateRoleRequest dto) {
        RoleResponse rr = roleService.create(dto);
        return new ResponseEntity<>(rr, HttpStatus.CREATED);
    }

    @Operation(summary = "Delete a single role", description = "Deletes a specific role by name. Requires Admin privileges.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Role not found", content = @Content)
    })
    @DeleteMapping(path = "/api/v1/roles/{name}")
    public ResponseEntity<GenericResponse> deleteOne(
            @Parameter(description = "The unique name of the role to delete") @PathVariable String name) {
        GenericResponse gr = roleService.deleteOne(name);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }

    @Operation(summary = "Delete multiple roles", description = "Deletes multiple roles specified in the request. Requires Admin privileges.",
            security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required", content = @Content),
            @ApiResponse(responseCode = "404", description = "One or more roles not found", content = @Content)
    })
    @DeleteMapping(path = "/api/v1/roles")
    public ResponseEntity<GenericResponse> deleteAllIn(@Valid @RequestBody DeleteRoleRequest roles) {
        GenericResponse gr = roleService.deleteAllIn(roles);
        return new ResponseEntity<>(gr, HttpStatus.OK);
    }
}
