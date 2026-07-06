package com.blogforge.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "bf_reaction")
public class Reaction extends AuditableEntity {

    @ManyToOne
    @JoinColumn(name = "reactor_id", nullable = false)
    private User reactor;

    @ManyToOne
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @Column(name = "reaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReactionType reactionType;

    public Reaction() {}

    public Reaction(User reactor, Blog blog, ReactionType reactionType) {
        this.reactor = reactor;
        this.blog = blog;
        this.reactionType = reactionType;
    }

    public User getReactor() {
        return reactor;
    }

    public void setReactor(User reactor) {
        this.reactor = reactor;
    }

    public Blog getBlog() {
        return blog;
    }

    public void setBlog(Blog blog) {
        this.blog = blog;
    }

    public ReactionType getReactionType() {
        return reactionType;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Reaction reaction = (Reaction) o;
        return Objects.equals(reactor, reaction.reactor) && Objects.equals(blog, reaction.blog) && reactionType == reaction.reactionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reactor, blog, reactionType);
    }
}
