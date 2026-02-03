package com.ecommerce.app.repository;

import com.ecommerce.app.dto.CategoryCountDTO;
import com.ecommerce.app.entity.Category;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("""
                SELECT new com.ecommerce.app.dto.CategoryCountDTO(c.id, c.name, c.imageUrl, COUNT(p))
                FROM Category c
                LEFT JOIN c.products p
                GROUP BY c.id, c.name, c.imageUrl
            """)
    List<CategoryCountDTO> findAllWithProductCount();
}
