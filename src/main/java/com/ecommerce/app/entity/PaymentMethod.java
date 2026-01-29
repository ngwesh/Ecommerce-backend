package com.ecommerce.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type; // CARD, MPESA, PAYPAL
    private String provider; // Visa, Mastercard, Safaricom
    private String last4Digits;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
