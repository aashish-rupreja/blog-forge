package com.blogforge.repository;

import com.blogforge.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Collection<Category> findByNameIn(Collection<String> categoryNames);
}
