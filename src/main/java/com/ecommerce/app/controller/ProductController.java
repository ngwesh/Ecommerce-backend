package com.ecommerce.app.controller;

import com.ecommerce.app.dto.ProductFilterRequest;
import com.ecommerce.app.entity.Category;
import com.ecommerce.app.entity.Product;
import com.ecommerce.app.repository.CategoryRepository;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.service.CloudinaryService;
import com.ecommerce.app.specification.ProductSpecification;

import io.jsonwebtoken.io.IOException;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
            @RequestParam("stock") Long stock,
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
            product.setStock(stock);
            Product savedProduct = productRepository.save(product);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);

        } catch (IOException e) {
            return buildErrorResponse("Image upload failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return buildErrorResponse("Error creating product: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/filter")
    public ResponseEntity<?> getFilteredProducts(@RequestBody ProductFilterRequest filterRequest) {
        try{
        Sort sort = Sort.unsorted();
        if (filterRequest.sort() != null) {
            sort = switch (filterRequest.sort()) {
                case "price_low" -> Sort.by("price").ascending();
                case "price_high" -> Sort.by("price").descending();
                default -> Sort.by("id").descending();
            };
        }

        Specification<Product> spec = ProductSpecification.withFilters(filterRequest);
        return ResponseEntity.ok(productRepository.findAll(spec, sort));
    } catch(Exception e){
        return buildErrorResponse("Update failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!productRepository.existsById(id)) {
                response.put("success", false);
                response.put("message", "Product not found");

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            productRepository.deleteById(id);

            response.put("success", true);
            response.put("message", "{Product} deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete product");
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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

