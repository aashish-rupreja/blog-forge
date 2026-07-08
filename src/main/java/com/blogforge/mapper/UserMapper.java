package com.blogforge.mapper;

import com.blogforge.dto.BaseResponse;
import com.blogforge.dto.user.*;
import com.blogforge.entity.Role;
import com.blogforge.entity.User;
import com.blogforge.entity.UserStatus;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {

    public User fromCreateRequestToEntity(CreateUserRequest dto) {
        User u = new User();
        u.setFirstName(dto.firstName());
        u.setLastName(dto.lastName());
        u.setEmail(dto.email());
        u.setUsername(dto.username());
        u.setProfilePicLink(dto.profilePicLink());
        u.setBio(dto.bio());
        u.setPasswordHash(dto.password());
        u.setStatus(UserStatus.ENABLED);
        return u;
    }

    public UserSummaryResponse fromEntityToSummaryResponse(User u) {
        return new UserSummaryResponse(
                new BaseResponse(
                        u.getUuid(),
                        u.getCreatedAt(),
                        u.getUpdatedAt()),
                u.getFirstName(),
                u.getLastName(),
                u.getUsername(),
                u.getProfilePicLink(),
                u.getEmail(),
                u.getStatus(),
                u.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                u.getBlogs().size(),
                u.getComments().size(),
                u.getReactions().size()
        );
    }

    public UserProfileResponse fromEntityToProfileResponse(User u) {
        return new UserProfileResponse(
                new BaseResponse(
                        u.getUuid(),
                        u.getCreatedAt(),
                        u.getUpdatedAt()),
                u.getFirstName(),
                u.getLastName(),
                u.getUsername(),
                u.getProfilePicLink(),
                u.getBio()
        );
    }
}
