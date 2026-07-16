package com.blogforge.controller;

import com.blogforge.dto.LoginRequest;
import com.blogforge.security.JWTService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JWTController {

    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public JWTController(AuthenticationManager authenticationManager, JWTService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @GetMapping("/api/v1/auth/login")
    public String generateJwtToken(@Valid @RequestBody LoginRequest loginRequest) {

        String username = loginRequest.username();
        String password = loginRequest.password();

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        return jwtService.genetateJwtToken(authentication);
    }
}
