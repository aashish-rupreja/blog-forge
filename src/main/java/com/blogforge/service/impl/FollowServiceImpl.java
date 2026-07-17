package com.blogforge.service.impl;

import com.blogforge.constants.Constants;
import com.blogforge.dto.GenericResponse;
import com.blogforge.entity.Follow;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.repository.FollowRepository;
import com.blogforge.repository.UserRepository;
import com.blogforge.service.FollowService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class FollowServiceImpl implements FollowService {

    private static final Logger LOG = LoggerFactory.getLogger(FollowServiceImpl.class);

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final MessageResolver messageResolver;

    public FollowServiceImpl(
            FollowRepository followRepository,
            UserRepository userRepository,
            MessageResolver messageResolver) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.messageResolver = messageResolver;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public GenericResponse create(String username, String authenticatedUsername) {
        LOG.trace("Entering create with username: {}, authenticatedUsername: {}", username, authenticatedUsername);
        // check if user is attempting self follow
        if (authenticatedUsername.equals(username)) {
            String selfFollowNotAllowed = messageResolver.getMessage("follow.self.not-allowed");
            LOG.warn(selfFollowNotAllowed);
            throw new IllegalStateException(selfFollowNotAllowed);
        }

        // check if author even exists
        LOG.trace("Finding author by username: {}", username);
        User author = userRepository.findByUsernameAndRoles_Name(username, Constants.AUTHOR_ROLE_NAME)
                .orElseThrow(() -> {
                    String authorNotExists = messageResolver.getMessage(
                            "entity.not-found",
                            "Author", username
                    );
                    LOG.warn(authorNotExists);
                    return new EntityNotFoundException(authorNotExists);
                });

        // check if current user is already following the author
        LOG.trace("Checking if follower {} is already following following {}", authenticatedUsername, username);
        boolean alreadyFollowing = followRepository.existsByFollower_UsernameAndFollowing_Username(
                authenticatedUsername, username);
        if (alreadyFollowing) {
            String alreadyFollowingAuthor = messageResolver.getMessage(
                    "follow.already-following",
                    username);
            LOG.warn(alreadyFollowingAuthor);
            throw new IllegalStateException(alreadyFollowingAuthor);
        }

        LOG.trace("Finding follower user by username: {}", authenticatedUsername);
        User authenticatedUser = userRepository.findByUsernameIgnoreCase(authenticatedUsername)
                .orElseThrow(() -> {
                    String userNotExists = messageResolver.getMessage(
                            "entity.not-found",
                            "User", username
                    );
                    LOG.warn(userNotExists);
                    return new EntityNotFoundException(userNotExists);
                });

        // finally create the follow
        Follow f = new Follow();
        f.setFollowing(author);
        f.setFollower(authenticatedUser);
        f.setFollowedAt(Instant.now());
        
        LOG.trace("Saving new follow to repository");
        followRepository.save(f);

        String followSuccessful = messageResolver.getMessage("follow.created", username);
        LOG.trace("Exiting create with message: {}", followSuccessful);
        return new GenericResponse(followSuccessful);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public GenericResponse delete(String username, String authenticatedUsername) {
        LOG.trace("Entering delete with username: {}, authenticatedUsername: {}", username, authenticatedUsername);

        if (authenticatedUsername.equals(username)) {
            String selfUnfollow = messageResolver.getMessage("unfollow.self.not-allowed");
            LOG.warn(selfUnfollow);
            throw new IllegalStateException(selfUnfollow);
        }

        LOG.trace("Deleting follow record from repository between follower {} and following {}", authenticatedUsername, username);
        int d = followRepository.deleteByFollower_UsernameAndFollowing_Username(authenticatedUsername, username);
        String unfollowMsg = (d > 0)
                ? messageResolver.getMessage("follow.removed", username)
                : messageResolver.getMessage("unfollow.not-following", username);

        LOG.info(unfollowMsg);
        LOG.trace("Exiting delete with message: {}", unfollowMsg);
        return new GenericResponse(unfollowMsg);
    }
}
