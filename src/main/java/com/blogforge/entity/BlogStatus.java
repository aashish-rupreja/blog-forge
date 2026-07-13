package com.blogforge.entity;

import java.util.Set;

public enum BlogStatus {
    DRAFT(Set.of("PUBLISHED", "DELETED")),
    PUBLISHED(Set.of("ARCHIVED", "DELETED")),
    ARCHIVED(Set.of("PUBLISHED", "DELETED")),
    DELETED(Set.of("PUBLISHED", "ARCHIVED", "DRAFT"));

    private final Set<String> allowedTransitions;

    BlogStatus(Set<String> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(String transition) {
        return allowedTransitions.contains(transition);
    }
}
