package com.blogforge.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "bf_author_application")
public class AuthorApplication extends AuditableEntity {

    @ManyToOne
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne
    @JoinColumn(name = "application_reviewer_id", nullable = false)
    private User applicationReviewer;

    @Column(name = "application_reason", length = 500, nullable = false)
    private String applicationReason;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AuthorApplicationStatus status;

    @Column(name = "reviewer_remarks", length = 100)
    private String reviewerRemarks;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    public AuthorApplication() {}

    public AuthorApplication(User applicant, User applicationReviewer, String applicationReason, AuthorApplicationStatus status, String reviewerRemarks, Instant reviewedAt) {
        this.applicant = applicant;
        this.applicationReviewer = applicationReviewer;
        this.applicationReason = applicationReason;
        this.status = status;
        this.reviewerRemarks = reviewerRemarks;
        this.reviewedAt = reviewedAt;
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

    public String getReviewerRemarks() {
        return reviewerRemarks;
    }

    public void setReviewerRemarks(String reviewerRemarks) {
        this.reviewerRemarks = reviewerRemarks;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
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
