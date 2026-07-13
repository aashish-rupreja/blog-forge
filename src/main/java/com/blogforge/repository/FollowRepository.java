package com.blogforge.repository;

import com.blogforge.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

    boolean existsByFollower_UsernameAndFollowing_Username(String followerUsername, String followingUsername);
}
