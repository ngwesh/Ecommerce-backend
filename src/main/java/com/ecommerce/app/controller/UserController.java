package com.ecommerce.app.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.app.entity.User;
import com.ecommerce.app.repository.UserRepository;
import com.ecommerce.app.security.JwtUtil;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepo, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
   public ResponseEntity<?> register(@RequestBody User user) {
    // Check if the email is already taken
    if (userRepo.findByEmail(user.getEmail()).isPresent()) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409 Conflict
                .body("Error: Email is already in use!");
    }

    try {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        User savedUser = userRepo.save(user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        
    } catch (Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Error
                .body("An unexpected error occurred: " + e.getMessage());
    }
}

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody User user) {
    try {
        //Check if the user exists
        Optional<User> existing = userRepo.findByEmail(user.getEmail());
        
        if (existing.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is not registered!");
        }

        //Validate password
        if (passwordEncoder.matches(user.getPassword(), existing.get().getPassword())) {
            String token = jwtUtil.generateToken(existing.get());
            return ResponseEntity.ok(token);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid credentials");

    } catch (Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An error occurred during login: " + e.getMessage());
    }
}
}

