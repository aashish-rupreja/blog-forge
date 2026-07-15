package com.blogforge.repository;

import com.blogforge.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    Optional<Reaction> findByReactor_UsernameAndBlog_Slug(String reactorUsername, String blogSlug);
}
