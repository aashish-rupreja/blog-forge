package com.blogforge.specification.user;

import com.blogforge.entity.User;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> usernameContains(String providedUsername) {
        return new Specification<User>() {
            @Override
            public @Nullable Predicate toPredicate(Root<User> userTable, CriteriaQuery<?> query, CriteriaBuilder queryBuilder) {
                return queryBuilder.like(
                        queryBuilder.lower(userTable.get("username")),
                        "%"+providedUsername+"%"
                );
            }
        };
    }

    public static Specification<User> handleSpecs(UserSpecificationParams specParams) {
        Specification<User> userSpec = Specification.unrestricted();
        if(specParams.username() != null) {
            userSpec = userSpec.and(usernameContains(specParams.username().toLowerCase()));
        }
        return userSpec;
    }
}
