package com.blogforge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "bf_user")
public class User extends AuditableEntity{

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "profile_pic_link")
    private String profilePicLink;

    @Column(name = "bio")
    private String bio;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    private Set<Role> roles = new HashSet<>();
    private Set<Blog> blogs = new HashSet<>();
    private Set<Comment> comments = new HashSet<>();
    private Set<Reaction> reactions = new HashSet<>();

    public User () {}

    public User(String firstName, String lastName, String email, String profilePicLink, String bio, String passwordHash, Set<Role> roles, Set<Blog> blogs, Set<Comment> comments, Set<Reaction> reactions) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profilePicLink = profilePicLink;
        this.bio = bio;
        this.passwordHash = passwordHash;
        this.roles = roles;
        this.blogs = blogs;
        this.comments = comments;
        this.reactions = reactions;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicLink() {
        return profilePicLink;
    }

    public void setProfilePicLink(String profilePicLink) {
        this.profilePicLink = profilePicLink;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Blog> getBlogs() {
        return blogs;
    }

    public void setBlogs(Set<Blog> blogs) {
        this.blogs = blogs;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Set<Reaction> getReactions() {
        return reactions;
    }

    public void setReactions(Set<Reaction> reactions) {
        this.reactions = reactions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(email);
    }
}
