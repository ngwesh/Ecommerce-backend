package com.ecommerce.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCountDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Long productCount;

    public CategoryCountDTO(Long id, String name, String imageUrl, Long productCount) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.productCount = productCount;
    }
}
