package com.blogforge.repository;

import com.blogforge.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Collection<Tag> findByNameIn(Collection<String> tagNames);
}
