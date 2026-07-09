package com.blogforge.specification.category;

import com.blogforge.entity.Category;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.data.jpa.domain.Specification;

public class CategorySpecification {

    public static Specification<Category> containsName(String providedName) {
        return new Specification<Category>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Category> categoryTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.like(
                        queryBuilder.lower(categoryTable.get("name")),
                        "%"+providedName+"%"
                );
            }
        };
    }

    public static Specification<Category> handleSpecs(CategorySpecificationParams specParams) {
        Specification<Category> spec = Specification.unrestricted();
        if(specParams.name() != null) {
            spec = spec.and(containsName(specParams.name()));
        }

        return spec;
    }
}
