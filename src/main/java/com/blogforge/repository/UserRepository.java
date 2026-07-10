package com.blogforge.repository;

import com.blogforge.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username, Specification<User> userSpec);

    Optional<User> findByUsernameAndRoles_Name(String username, String roleName);

    Page<User> findAllByRoles_Name(String roleName, Pageable pageable);
    Page<User> findAllByUsernameContainingIgnoreCaseAndRoles_Name(String username, String roleName, Pageable pageable);
}
