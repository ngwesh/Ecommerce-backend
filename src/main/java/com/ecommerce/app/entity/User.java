package com.ecommerce.app.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")


public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String password;
    private String role; // USER, ADMIN

    @OneToMany(mappedBy = "user")
    private List<Address> addresses;
    
    @OneToMany(mappedBy = "user")
    private List<PaymentMethod> paymentMethods;
}
