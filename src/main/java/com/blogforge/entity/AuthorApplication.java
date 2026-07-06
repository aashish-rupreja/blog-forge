package com.blogforge.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "author_application")
public class AuthorApplication extends AuditableEntity {

    private User applicant;

    private User applicationReviewer;

    @Column(name = "application_reason", length = 300)
    private String applicationReason;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AuthorApplicationStatus status;

    public AuthorApplication() {}

    public AuthorApplication(User applicant, User applicationReviewer, String applicationReason, AuthorApplicationStatus status) {
        this.applicant = applicant;
        this.applicationReviewer = applicationReviewer;
        this.applicationReason = applicationReason;
        this.status = status;
    }

    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

    public User getApplicationReviewer() {
        return applicationReviewer;
    }

    public void setApplicationReviewer(User applicationReviewer) {
        this.applicationReviewer = applicationReviewer;
    }

    public String getApplicationReason() {
        return applicationReason;
    }

    public void setApplicationReason(String applicationReason) {
        this.applicationReason = applicationReason;
    }

    public AuthorApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(AuthorApplicationStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AuthorApplication that = (AuthorApplication) o;
        return Objects.equals(applicant, that.applicant) && Objects.equals(applicationReason, that.applicationReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicant, applicationReason);
    }
}
