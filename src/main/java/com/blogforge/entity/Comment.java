package com.blogforge.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "bf_comment")
public class Comment extends AuditableEntity {

    private User owner;

    @Column(name = "content")
    private String content;

    private Blog blog;

    public Comment () {}

    public Comment(User owner, String content, Blog blog) {
        this.owner = owner;
        this.content = content;
        this.blog = blog;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(owner, comment.owner) && Objects.equals(content, comment.content) && Objects.equals(blog, comment.blog);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, content, blog);
    }
}
