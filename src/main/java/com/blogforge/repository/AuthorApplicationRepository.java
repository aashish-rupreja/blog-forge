package com.blogforge.repository;

import com.blogforge.entity.AuthorApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthorApplicationRepository extends JpaRepository<AuthorApplication, UUID> {
}
