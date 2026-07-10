package com.blogforge.repository;

import com.blogforge.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Collection<Tag> findByNameIn(Collection<String> tagNames);

    @Query(
            value = """
                SELECT t.uuid, t.name, t.created_at, t.updated_at FROM bf_tag t WHERE t.name ILIKE %?1%
            """,
            nativeQuery = true
    )
    Page<Tag> findByNameContaining(Pageable pageable, String name);

    Optional<Tag> findByNameIgnoreCase(String tagName);
}
