package com.blogforge.repository;

import com.blogforge.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


import java.util.Optional;
import java.util.UUID;

public interface BlogRepository extends JpaRepository<Blog, UUID>, JpaSpecificationExecutor<Blog> {

    Optional<Blog> findBySlugIgnoreCase(String slug);

    Page<Blog> findAllByAuthor_UsernameIgnoreCase(String authorUsername, Pageable pageable);

    @Query(value = """
            SELECT bfb.slug FROM bf_blog bfb WHERE bfb.slug ILIKE (:prefix%) ORDER BY bfb.slug DESC
    """, nativeQuery = true)
    Optional<String> findLatestSlugWithPrefix(String prefix);
}
