package com.blogforge.service.impl;

import com.blogforge.dto.GenericResponse;
import com.blogforge.entity.Follow;
import com.blogforge.entity.Role;
import com.blogforge.entity.User;
import com.blogforge.exception.MessageResolver;
import com.blogforge.repository.FollowRepository;
import com.blogforge.repository.UserRepository;
import com.blogforge.service.FollowService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowService {

    private final String AUTHOR_ROLE_NAME = "ROLE_AUTHOR";

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
    public GenericResponse create(String username) {
        // check if author even exists
        Optional<User> checkAuthor = userRepository.findByUsernameIgnoreCase(username);
        if(checkAuthor.isEmpty()) {
            String authorNotExists = messageResolver.getMessage(
                "entity.not-found",
                    "Author", username
            );
            throw new EntityNotFoundException(authorNotExists);
        }

        // check if this user is REALLY an author
        User author = checkAuthor.get();
        Optional<Role> checkRole = author.getRoles()
                .stream()
                .filter(r -> r.getName().equals(AUTHOR_ROLE_NAME))
                .findFirst();
        if(checkRole.isEmpty()) {
            String nonAuthorFollow = messageResolver.getMessage("follow.non-author.not-allowed");
            throw new IllegalStateException(nonAuthorFollow);
        }

        // check if user is attempting self follow
        String currentAuthenticatedUsername = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if(currentAuthenticatedUsername.equals(username)) {
            String selfFollowNotAllowed = messageResolver.getMessage("follow.self.not-allowed");
            throw new IllegalStateException(selfFollowNotAllowed);
        }

        // at this point the current user will always exist hence .get() directly
        User currentUser = userRepository.findByUsernameIgnoreCase(currentAuthenticatedUsername).get();

        // check if current user is already following the author
        boolean alreadyFollowing = followRepository.existsByFollower_UsernameAndFollowing_Username(
                currentAuthenticatedUsername,
                username
        );
        if(alreadyFollowing) {
            String alreadyFollowingAuthor = messageResolver.getMessage(
                    "follow.already-following",
                    username
            );
            throw new IllegalStateException(alreadyFollowingAuthor);
        }

        // finally create the follow
        Follow f = new Follow();
        f.setFollowing(author);
        f.setFollower(currentUser);
        f.setFollowedAt(Instant.now());
        followRepository.save(f);

        String followSuccessful = messageResolver.getMessage("follow.successful", username);
        return new GenericResponse(followSuccessful);
    }

    @Override
    @Transactional
    public GenericResponse delete(String username) {
        String currentAuthenticatedUsername = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        if(currentAuthenticatedUsername.equals(username)) {
            String selfUnfollow = messageResolver.getMessage("unfollow.self.not-allowed");
            throw new IllegalStateException(selfUnfollow);
        }

        int d = followRepository.deleteByFollower_UsernameAndFollowing_Username(currentAuthenticatedUsername, username);
        String msg = (d > 0)
                ? messageResolver.getMessage("unfollow.successful", username)
                : messageResolver.getMessage("unfollow.unnecessary", username);

        return new GenericResponse(msg);
    }
}
