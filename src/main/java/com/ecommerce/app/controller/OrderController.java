package com.ecommerce.app.controller;

import org.springframework.web.bind.annotation.*;

import com.ecommerce.app.entity.Order;
import com.ecommerce.app.messaging.OrderProducer;
import com.ecommerce.app.repository.OrderRepository;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepo;
    private final OrderProducer producer;

    public OrderController(OrderRepository orderRepo, OrderProducer producer) {
        this.orderRepo = orderRepo;
        this.producer = producer;
    }

    @PostMapping
    public Order createOrder(@RequestBody Order order) {
        order.setStatus("CREATED");
        Order saved = orderRepo.save(order);
        producer.sendOrderCreated(saved);
        return saved;
    }
}
