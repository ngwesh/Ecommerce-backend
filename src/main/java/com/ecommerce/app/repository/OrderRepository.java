package com.ecommerce.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.app.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
