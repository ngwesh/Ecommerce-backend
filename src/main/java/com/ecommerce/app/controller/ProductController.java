package com.ecommerce.app.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.app.entity.Product;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.service.CloudinaryService;

import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    public ProductController(ProductRepository productRepository,
                             CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Product createProduct(
            @RequestPart("product") Product product,
            @RequestPart("image") MultipartFile image
    ) throws IOException {

        String imageUrl = cloudinaryService.uploadImage(image);
        product.setImageUrl(imageUrl);

        return productRepository.save(product);
    }
}

