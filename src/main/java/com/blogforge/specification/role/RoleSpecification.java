package com.blogforge.specification.role;

import com.blogforge.entity.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

public class RoleSpecification {

    public static Specification<Role> nameContains(String providedName) {
        return new Specification<Role>() {
            @Override
            public @Nullable Predicate toPredicate(Root<Role> roleTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.like(
                        queryBuilder.lower(roleTable.get("name")),
                        "%" + providedName + "%"
                );
            }
        };
    }

    public static Specification<Role> handleSpecs(RoleSpecificationParams specParams) {
        Specification<Role> spec = Specification.unrestricted();
        if(specParams.name() != null) {
            spec = spec.and(nameContains(specParams.name()));
        }

        return spec;
    }
}
