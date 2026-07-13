package com.blogforge.specification.authorapplication;

import com.blogforge.dto.authorapplication.MyAuthorApplicationsRequest;
import com.blogforge.entity.AuthorApplication;
import com.blogforge.entity.AuthorApplicationStatus;
import jakarta.persistence.criteria.*;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;


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

    public static Specification<AuthorApplication> applicantUsernameIs(String username) {
        return new Specification<AuthorApplication>() {
            @Override
            public @Nullable Predicate toPredicate(
                    Root<AuthorApplication> authorApplicationTable,
                    CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {

                return queryBuilder.equal(
                        authorApplicationTable.join("applicant").get("username"),
                        username
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

    public static Specification<AuthorApplication> reviewedBefore(String reviewedBeforeDate) {
        return new Specification<AuthorApplication>() {
            @Override
            public @Nullable Predicate toPredicate(
                    Root<AuthorApplication> authorApplicationTable,
                    CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {

                return queryBuilder.lessThan(
                        authorApplicationTable.get("reviewedAt"),
                        LocalDate.parse(reviewedBeforeDate)
                );
            }
        };
    }

    public static Specification<AuthorApplication> reviewedOn(String reviewedOnDate) {
        return new Specification<AuthorApplication>() {
            @Override
            public @Nullable Predicate toPredicate(
                    Root<AuthorApplication> authorApplicationTable,
                    CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {

                return queryBuilder.between(
                        authorApplicationTable.get("reviewedAt"),
                        LocalDate.parse(reviewedOnDate),
                        LocalDate.parse(reviewedOnDate).plusDays(1)
                );
            }
        };
    }

    public static Specification<AuthorApplication> reviewedAfter(String reviewedAfterDate) {
        return new Specification<AuthorApplication>() {
            @Override
            public @Nullable Predicate toPredicate(
                    Root<AuthorApplication> authorApplicationTable,
                    CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {

                return queryBuilder.greaterThan(
                        authorApplicationTable.get("reviewedAt"),
                        LocalDate.parse(reviewedAfterDate).plusDays(1)
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

    public static Specification<AuthorApplication> handleMyApplicationSpecs(MyAuthorApplicationsRequest specParams, String username) {
        Specification<AuthorApplication> spec = Specification.unrestricted();
        spec = spec.and(applicantUsernameIs(username.toLowerCase()));

        if(specParams.status() != null) {
            spec = spec.and(statusIs(specParams.status()));
        }
        if(specParams.reviewedBeforeDate() != null) {
            spec = spec.and(reviewedBefore(specParams.reviewedBeforeDate()));
        }
        if(specParams.reviewedOnDate() != null) {
            spec = spec.and(reviewedOn(specParams.reviewedOnDate()));
        }
        if(specParams.reviewedAfterDate() != null) {
            spec = spec.and(reviewedAfter(specParams.reviewedAfterDate()));
        }

        return spec;
    }
}
