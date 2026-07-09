package com.blogforge.specification.authorapplication;

import com.blogforge.entity.AuthorApplication;
import com.blogforge.entity.AuthorApplicationStatus;
import jakarta.persistence.criteria.*;
import org.hibernate.grammars.ordering.OrderingParser;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;


public class AuthorApplicationSpecification {

    public static Specification<AuthorApplication> applicantUsernameContains(String username) {
        return new Specification<AuthorApplication>() {
            @Override
            public @Nullable Predicate toPredicate(
                    Root<AuthorApplication> authorApplicationTable,
                    CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.like(
                        authorApplicationTable.join("applicant").get("username"),
                        "%"+username.toLowerCase()+"%"
                );
            }
        };
    }

    public static Specification<AuthorApplication> reviewerUsernameContains(String reviewerUsername) {
        return new Specification<AuthorApplication>() {
            @Override
            public @Nullable Predicate toPredicate(
                    Root<AuthorApplication> authorApplicationTable,
                    CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.like(
                        authorApplicationTable.join("applicationReviewer").get("username"),
                        "%"+reviewerUsername.toLowerCase()+"%"
                );
            }
        };
    }

    public static Specification<AuthorApplication> statusIs(AuthorApplicationStatus status) {
        return new Specification<AuthorApplication>() {
            @Override
            public @Nullable Predicate toPredicate(
                    Root<AuthorApplication> authorApplicationTable,
                    CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.equal(
                        authorApplicationTable.get("status"),
                        status
                );
            }
        };
    }

    public static Specification<AuthorApplication> handleSpecs(AuthorApplicationSpecificationParams specParams) {
        Specification<AuthorApplication> spec = Specification.unrestricted();
        if(specParams.applicantUsername() != null) {
            spec = spec.and(applicantUsernameContains(specParams.applicantUsername()));
        }
        if(specParams.applicationReviewerUsername() != null) {
            spec = spec.and(reviewerUsernameContains(specParams.applicationReviewerUsername()));
        }
        if(specParams.status() != null) {
            spec = spec.and(statusIs(specParams.status()));
        }

        return spec;
    }
}
