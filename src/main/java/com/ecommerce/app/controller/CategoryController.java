package com.ecommerce.app.controller;

import com.ecommerce.app.entity.Category;
import com.ecommerce.app.repository.CategoryRepository;
import com.ecommerce.app.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {
    @Autowired
    CloudinaryService cloudinaryService;

    @Autowired
    CategoryRepository categoryRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCategory(
            @RequestParam("name") String name,
            @RequestPart(value = "image", required = true) MultipartFile image) {
        try {
            Category category = new Category();
            if (image != null) {
                category.setImageUrl(cloudinaryService.uploadImage(image));
            }
            category.setName(name);
            return ResponseEntity.status(HttpStatus.CREATED).body(categoryRepository.save(category));
        } catch (Exception e) {
            return buildErrorResponse("Creation failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    //GET ALL
    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        return ResponseEntity.ok(categoryRepository.findAllWithProductCount());
    }

    //GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        
        if (categoryOpt.isEmpty()) {
            return buildErrorResponse("Category not found", HttpStatus.NOT_FOUND);
        }
        
        return ResponseEntity.ok(categoryOpt.get());
    }
    //UPDATE
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateCategory(
            @PathVariable Long id,
            @RequestPart("category") Category categoryDetails,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);

            if (categoryOpt.isEmpty()) {
                return buildErrorResponse("Category not found", HttpStatus.NOT_FOUND);
            }

            Category existingCategory = categoryOpt.get();
            existingCategory.setName(categoryDetails.getName());

            if (image != null && !image.isEmpty()) {
                String imageUrl = cloudinaryService.uploadImage(image);
                existingCategory.setImageUrl(imageUrl);
            }

            Category updated = categoryRepository.save(existingCategory);
            return ResponseEntity.ok(updated);

        } catch (IOException e) {
            return buildErrorResponse("Update failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            Optional<Category> categoryOpt = categoryRepository.findById(id);
            if (categoryOpt.isEmpty()) {
                return buildErrorResponse("Category not found", HttpStatus.NOT_FOUND);
            }
            categoryRepository.delete(categoryOpt.get());
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return buildErrorResponse("Delete failed. Check if products are linked to this category.", HttpStatus.CONFLICT);
        }
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, HttpStatus status) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", System.currentTimeMillis());
        error.put("message", message);
        error.put("status", status.value());
        return new ResponseEntity<>(error, status);
    }
}
