package com.blogforge.repository;

import com.blogforge.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID>, JpaSpecificationExecutor<Role> {

    Optional<Role> findByNameIgnoreCase(String name);

    @Modifying
    @Query("""
        DELETE FROM Role r WHERE UPPER(r.name) IN :roleNames
    """)
    long deleteAllIn(Collection<String> roleNames);
}
