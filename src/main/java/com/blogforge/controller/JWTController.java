package com.blogforge.controller;

import com.blogforge.dto.jwt.JwtResponse;
import com.blogforge.dto.jwt.LoginRequest;
import com.blogforge.security.JWTService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for user authentication and JWT token generation")
@RestController
public class JWTController {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public JWTController(AuthenticationManager authenticationManager, JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Login and get JWT token", description = "Authenticates a user with username and password, and returns a signed JWT token upon success")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully authenticated",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request / validation error", content = @Content)
    })
    @PostMapping("/api/v1/auth/login")
    public ResponseEntity<JwtResponse> generateJwtToken(@Valid @RequestBody LoginRequest loginRequest) {

        String username = loginRequest.username();
        String password = loginRequest.password();

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        JwtResponse jwtResponse = new JwtResponse(jwtService.genetateJwtToken(authentication));
        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }
}
