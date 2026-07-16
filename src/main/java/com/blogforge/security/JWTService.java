package com.blogforge.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
public class JWTService {

    private static final Logger LOG = LoggerFactory.getLogger(JWTService.class);

    private final String JWT_SECRET;

    public JWTService(@Value("${jwt.secret.key}") String JWT_SECRET) {
        this.JWT_SECRET = JWT_SECRET;
    }

    public String genetateJwtToken(Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(customUserDetails.getUsername())
                .claim(
                        "roles",
                        customUserDetails.getAuthorities()
                                .stream()
                                .map(ga -> ga.getAuthority())
                                .toList()
                )
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(Duration.ofMinutes(30))))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            LOG.warn("JWT Parse error:");
            LOG.warn(e.getClass().getName()+" "+e.getMessage());
            throw e;
        }
    }

    public List<? extends GrantedAuthority> extractAuthorities(String token) {
        List<String> roles = (List<String>) extractAllClaims(token);
        return roles.stream()
                .map(s -> new SimpleGrantedAuthority(s))
                .toList();

    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }


}
