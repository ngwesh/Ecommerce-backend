package com.ecommerce.app.controller;

import com.ecommerce.app.entity.Product;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.service.CloudinaryService;
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
    private final CloudinaryService cloudinaryService;

    public ProductController(ProductRepository productRepository, CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.cloudinaryService = cloudinaryService;
    }

    //CREATE
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestPart("product") Product product,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image);
                product.setImageUrl(imageUrl);
            }
            Product savedProduct = productRepository.save(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (Exception e) {
            return buildErrorResponse("Error creating product: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //READ ALL
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = productRepository.findAll();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return buildErrorResponse("Could not fetch products", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

// READ ONE
@GetMapping("/{id}")
public ResponseEntity<?> getProductById(@PathVariable Long id) {
    try {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            return buildErrorResponse("Product not found with id: " + id, HttpStatus.NOT_FOUND);
        }
    } catch (Exception e) {
        return buildErrorResponse("Error fetching product", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

// UPDATE - Fixed
@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<?> updateProduct(
        @PathVariable Long id,
        @RequestPart("product") Product productDetails,
        @RequestPart(value = "image", required = false) MultipartFile image) {
    try {
        Optional<Product> productOptional = productRepository.findById(id);
        
        if (productOptional.isEmpty()) {
            return buildErrorResponse("Product not found with id: " + id, HttpStatus.NOT_FOUND);
        }

        Product existingProduct = productOptional.get();
        existingProduct.setName(productDetails.getName());
        existingProduct.setPrice(productDetails.getPrice());
        existingProduct.setDescription(productDetails.getDescription());

        if (image != null && !image.isEmpty()) {
            String newImageUrl = cloudinaryService.uploadImage(image);
            existingProduct.setImageUrl(newImageUrl);
        }

        Product updatedProduct = productRepository.save(existingProduct);
        return ResponseEntity.ok(updatedProduct);
        
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
        return new ResponseEntity<>(error, status);
    }
}