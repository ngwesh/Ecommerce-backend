package com.ecommerce.app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ecommerce.app.entity.Order;
import com.ecommerce.app.entity.OrderItem;
import com.ecommerce.app.messaging.OrderProducer;
import com.ecommerce.app.repository.OrderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

   @Autowired
   OrderRepository orderRepo;
   @Autowired
   OrderProducer producer;

    // CREATE
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Order order) {
        Map<String, Object> response = new HashMap<>();

        try {
            order.setStatus("CREATED");

            // link items to order
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    item.setOrder(order);
                }
            }

            Order saved = orderRepo.save(order);
            producer.sendOrderCreated(saved);

            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("data", saved);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to create order");
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // GET ALL
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Order> orders = orderRepo.findAll();

            response.put("success", true);
            response.put("data", orders);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to fetch orders");
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Order> order = orderRepo.findById(id);

            if (order.isEmpty()) {
                response.put("success", false);
                response.put("message", "Order not found");

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            response.put("success", true);
            response.put("data", order.get());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to fetch order");
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOrder(
            @PathVariable Long id,
            @RequestBody Order updatedOrder) {

        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Order> existingOpt = orderRepo.findById(id);

            if (existingOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Order not found");

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Order existing = existingOpt.get();

            existing.setStatus(updatedOrder.getStatus());
            existing.setTotal(updatedOrder.getTotal());
            existing.setUser(updatedOrder.getUser());

            // replace items
            if (updatedOrder.getItems() != null) {
                for (OrderItem item : updatedOrder.getItems()) {
                    item.setOrder(existing);
                }
                existing.setItems(updatedOrder.getItems());
            }

            Order saved = orderRepo.save(existing);

            response.put("success", true);
            response.put("message", "Order updated successfully");
            response.put("data", saved);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update order");
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!orderRepo.existsById(id)) {
                response.put("success", false);
                response.put("message", "Order not found");

                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            orderRepo.deleteById(id);

            response.put("success", true);
            response.put("message", "Order deleted successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to delete order");
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

