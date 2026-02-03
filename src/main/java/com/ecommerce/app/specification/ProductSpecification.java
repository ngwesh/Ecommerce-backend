package com.ecommerce.app.specification;

import com.ecommerce.app.dto.ProductFilterRequest;
import com.ecommerce.app.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {
    public static Specification<Product> withFilters(ProductFilterRequest filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filters.categories() != null && !filters.categories().isEmpty()) {
                predicates.add(root.get("category").get("id").in(filters.categories()));
            }

            // if (filters.ratings() != null && !filters.ratings().isEmpty()) {
            //     predicates.add(root.get("rating").in(filters.ratings()));
            // }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}