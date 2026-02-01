package com.ecommerce.app.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {
    // 1. Upload the file and get the response map
    Map uploadResult = cloudinary.uploader()
            .upload(file.getBytes(), ObjectUtils.emptyMap());

    String imageUrl = uploadResult.get("secure_url").toString();
    System.out.println("Cloudinary Upload URL: " + imageUrl);

    return imageUrl;
}
}
