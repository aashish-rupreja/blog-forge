package com.blogforge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(!request.getHeader("Authorization").startsWith("Bearer")) {
            doFilter(request, response, filterChain);
        }

        String token = request.getHeader("Authorization").substring(7);

        // if token invalid do filter

        // else extract payload, username and roles
        // create authentication object and set it in security context
        // doFilter ahead

    }
}
