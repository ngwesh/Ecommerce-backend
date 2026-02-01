package com.ecommerce.app.controller;

import com.ecommerce.app.entity.Category;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.repository.CategoryRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.service.CloudinaryService;

import io.jsonwebtoken.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    public ProductController(ProductRepository productRepository,
            CategoryRepository categoryRepository,
            CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.cloudinaryService = cloudinaryService;
    }

    //CREATE
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") String price,
            @RequestParam("categoryId") Long categoryId,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            Optional<Category> categoryOptional = categoryRepository.findById(categoryId);

            if (categoryOptional.isEmpty()) {
                return buildErrorResponse("Category not found with ID: " + categoryId, HttpStatus.NOT_FOUND);
            }

            Product product = new Product();

            Category category = categoryOptional.get();
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image);
                product.setImageUrl(imageUrl);
            }

            product.setCategory(category);
            product.setName(name);
            product.setDescription(description);
            product.setPrice(Double.parseDouble(price));
            Product savedProduct = productRepository.save(product);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);

        } catch (IOException e) {
            return buildErrorResponse("Image upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Error creating product: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //GET ALL
    @GetMapping
    public ResponseEntity<List<Product>> getAllCategories() {
        return ResponseEntity.ok(productRepository.findAll());
    }

    // UPDATE
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestPart("product") Product productDetails,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            Optional<Product> productOptional = productRepository.findById(id);

            if (productOptional.isEmpty()) {
                return buildErrorResponse("Product not found", HttpStatus.NOT_FOUND);
            }

            Product existingProduct = productOptional.get();
            existingProduct.setName(productDetails.getName());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setDescription(productDetails.getDescription());

            // Update category if a new ID is provided
            if (categoryId != null) {
                categoryRepository.findById(categoryId).ifPresent(existingProduct::setCategory);
            }

            // Update image if provided
            if (image != null && !image.isEmpty()) {
                try {
                    String newImageUrl = cloudinaryService.uploadImage(image);
                    existingProduct.setImageUrl(newImageUrl);
                } catch (IOException e) {
                    return buildErrorResponse("Image upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            return ResponseEntity.ok(productRepository.save(existingProduct));

        } catch (Exception e) {
            return buildErrorResponse("Update failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper method for consistent JSON error responses
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", System.currentTimeMillis());
        error.put("message", message);
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        return new ResponseEntity<>(error, status);
    }
}
