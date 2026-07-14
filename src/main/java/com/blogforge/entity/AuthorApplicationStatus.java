package com.blogforge.entity;

import java.util.Set;

public enum AuthorApplicationStatus {
    PENDING(Set.of("REJECTED", "APPROVED")),
    APPROVED(Set.of()),
    REJECTED(Set.of());

    private final Set<String> allowedTransitions;

    AuthorApplicationStatus(Set<String> allowedTransitions) {
        this.allowedTransitions = allowedTransitions;
    }

    public boolean canTransitionTo(String transitionTo) {
        return allowedTransitions.contains(transitionTo);
    }
}
