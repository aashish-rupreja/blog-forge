package com.blogforge.repository;

import com.blogforge.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID>, JpaSpecificationExecutor<Category> {
    Collection<Category> findByNameIn(Collection<String> categoryNames);

    Optional<Category> findByNameIgnoreCase(String categoryName);
}
