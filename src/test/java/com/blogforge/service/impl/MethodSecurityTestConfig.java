package com.blogforge.service.impl;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Minimal Spring configuration that enables method-level security.
 * Imported by authorization test slices to activate @PreAuthorize processing
 * without loading the full application context.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityTestConfig {
}
