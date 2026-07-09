package com.blogforge.specification.comment;

import com.blogforge.entity.Comment;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class CommentSpecification {

    public static Specification<Comment> containsUsername(String providedUsername) {
        return new Specification<Comment>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Comment> commentsTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.like(
                        queryBuilder.lower(commentsTable.join("owner").get("username")),
                        "%"+providedUsername+"%"
                );
            }
        };
    }

    public static Specification<Comment> containsComment(String providedComment) {
        return new Specification<Comment>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Comment> commentsTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.like(
                        queryBuilder.lower(commentsTable.get("content")),
                        "%"+providedComment+"%"
                );
            }
        };
    }

    public static Specification<Comment> postedBefore(String postedDate) {
        return new Specification<Comment>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Comment> commentsTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.lessThan(
                        commentsTable.get("createdAt"),
                        LocalDate.parse(postedDate)
                );
            }
        };
    }

    public static Specification<Comment> postedOn(String postedDate) {
        return new Specification<Comment>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Comment> commentsTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.between(
                                commentsTable.get("createdAt"),
                                LocalDate.parse(postedDate),
                                LocalDate.parse(postedDate).plusDays(1)
                );
            }
        };
    }

    public static Specification<Comment> postedAfter(String postedDate) {
        return new Specification<Comment>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Comment> commentsTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.greaterThan(
                        commentsTable.get("createdAt"),
                        LocalDate.parse(postedDate).plusDays(1)
                );
            }
        };
    }

    public static Specification<Comment> handleSpecs(CommentSpecificationParams specParams) {
        Specification<Comment> spec = Specification.unrestricted();

        if(specParams.owner() != null) {
            spec = spec.and(containsUsername(specParams.owner().toLowerCase()));
        }
        if(specParams.content() != null) {
            spec = spec.and(containsComment(specParams.content().toLowerCase()));
        }
        if(specParams.postedBefore() != null) {
            spec = spec.and(postedBefore(specParams.postedBefore()));
        }
        if(specParams.postedOn() != null) {
            spec = spec.and(postedOn(specParams.postedOn()));
        }
        if(specParams.postedAfter() != null) {
            spec = spec.and(postedAfter(specParams.postedAfter()));
        }

        return spec;
    }
}
