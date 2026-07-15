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
        // check if user is attempting self follow
        if (authenticatedUsername.equals(username)) {
            String selfFollowNotAllowed = messageResolver.getMessage("follow.self.not-allowed");
            LOG.warn(selfFollowNotAllowed);
            throw new IllegalStateException(selfFollowNotAllowed);
        }

        // check if author even exists
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
        boolean alreadyFollowing = followRepository.existsByFollower_UsernameAndFollowing_Username(
                authenticatedUsername, username);
        if (alreadyFollowing) {
            String alreadyFollowingAuthor = messageResolver.getMessage(
                    "follow.already-following",
                    username);
            LOG.warn(alreadyFollowingAuthor);
            throw new IllegalStateException(alreadyFollowingAuthor);
        }

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
        followRepository.save(f);

        String followSuccessful = messageResolver.getMessage("follow.created", username);
        return new GenericResponse(followSuccessful);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public GenericResponse delete(String username, String authenticatedUsername) {

        if (authenticatedUsername.equals(username)) {
            String selfUnfollow = messageResolver.getMessage("unfollow.self.not-allowed");
            LOG.warn(selfUnfollow);
            throw new IllegalStateException(selfUnfollow);
        }

        int d = followRepository.deleteByFollower_UsernameAndFollowing_Username(authenticatedUsername, username);
        String unfollowMsg = (d > 0)
                ? messageResolver.getMessage("follow.removed", username)
                : messageResolver.getMessage("unfollow.not-following", username);

        LOG.info(unfollowMsg);
        return new GenericResponse(unfollowMsg);
    }
}
