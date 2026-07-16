package com.blogforge.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JWTFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(request.getHeader("Authorization") == null || !request.getHeader("Authorization").startsWith("Bearer ")) {
            LOG.trace("Authorization header or Bearer token not present");
            doFilter(request, response, filterChain);
            return;
        }

        String token = request.getHeader("Authorization").substring(7);
        LOG.trace("Bearer token found");

        // if token invalid do filter

        // else extract payload, username and roles
        // create authentication object and set it in security context
        // doFilter ahead

    }
}
