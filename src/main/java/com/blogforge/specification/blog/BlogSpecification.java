package com.blogforge.specification.blog;

import com.blogforge.entity.Blog;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Set;

public class BlogSpecification {

    public static Specification<Blog> titleContains(String providedTitle) {
        System.out.println(providedTitle);
        return new Specification<Blog>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Blog> blogTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.like(
                        queryBuilder.lower(blogTable.get("title")),
                        "%"+providedTitle+"%"
                );
            }
        };
    }

    public static Specification<Blog> authorNameContains(String providedName) {
        return new Specification<Blog>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Blog> blogTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.like(
                        blogTable.join("author").get("username"),
                        "%"+providedName+"%"
                );
            }
        };
    }

    public static Specification<Blog> publishedAfter(String publishedAfter) {
        return new Specification<Blog>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Blog> blogTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.greaterThan(
                        blogTable.get("publishedAt"),
                        LocalDate.parse(publishedAfter).plusDays(1)
                );
            }
        };
    }

    public static Specification<Blog> publishedBefore(String publishedBefore) {
        return new Specification<Blog>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Blog> blogTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.lessThan(
                        blogTable.get("publishedAt"),
                        LocalDate.parse(publishedBefore)
                );
            }
        };
    }

    public static Specification<Blog> publishedOn(String publishedOn) {
        return new Specification<Blog>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Blog> blogTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.between(
                        blogTable.get("publishedAt"),
                        LocalDate.parse(publishedOn),
                        LocalDate.parse(publishedOn).plusDays(1)
                );
            }
        };
    }

    public static Specification<Blog> categoriesIn(Set<String> categories) {
        return new Specification<Blog>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Blog> blogTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.lower(blogTable.join("categories").get("name")).in(categories);
            }
        };
    }

    public static Specification<Blog> tagsIn(Set<String> tags) {
        return new Specification<Blog>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Blog> blogTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.lower(blogTable.join("tags").get("name")).in(tags);
            }
        };
    }

    public static Specification<Blog> handleSpecs(BlogSpecificationParams specParams) {
        Specification<Blog> spec = Specification.unrestricted();

        if(specParams.title() != null) {
            spec = spec.and(titleContains(specParams.title().toLowerCase()));
        }
        if(specParams.authorName() != null) {
            spec = spec.and(authorNameContains(specParams.authorName().toLowerCase()));
        }
        if(specParams.publishedAfter() != null) {
            spec = spec.and(publishedAfter(specParams.publishedAfter()));
        }
        if(specParams.publishedBefore() != null) {
            spec = spec.and(publishedBefore(specParams.publishedBefore()));
        }
        if(specParams.publishedOn() != null) {
            spec = spec.and(publishedOn(specParams.publishedOn()));
        }
        if(specParams.categories() != null && !specParams.categories().isEmpty()) {
            spec = spec.and(categoriesIn(specParams.categories()));
        }
        if(specParams.tags() != null && !specParams.tags().isEmpty()) {
            spec = spec.and(tagsIn(specParams.tags()));
        }

        return spec;
    }
}
