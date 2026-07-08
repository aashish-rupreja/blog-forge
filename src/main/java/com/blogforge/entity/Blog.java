package com.blogforge.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "bf_blog")
public class Blog extends AuditableEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", nullable = false)
    private String slug;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "enable_comments", nullable = false)
    private boolean enableComments;

    @Column(name = "blog_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BlogStatus status;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @ManyToMany
    @JoinTable(
            name = "bf_blog_category",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "bf_blog_tag",
            joinColumns = @JoinColumn(name = "blog_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "blog")
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "blog")
    private Set<Reaction> reactions = new HashSet<>();

    private Blog () {}

    public Blog(String title, String slug, String content, User author, boolean enableComments, BlogStatus status, Set<Category> categories, Set<Tag> tags, Set<Comment> comments, Set<Reaction> reactions) {
        this.title = title;
        this.slug = slug;
        this.content = content;
        this.author = author;
        this.enableComments = enableComments;
        this.status = status;
        this.categories = categories;
        this.tags = tags;
        this.comments = comments;
        this.reactions = reactions;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public boolean isEnableComments() {
        return enableComments;
    }

    public void setEnableComments(boolean enableComments) {
        this.enableComments = enableComments;
    }

    public BlogStatus getStatus() {
        return status;
    }

    public void setStatus(BlogStatus status) {
        this.status = status;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
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
        Blog blog = (Blog) o;
        return Objects.equals(title, blog.title) && Objects.equals(author, blog.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, author);
    }
}
