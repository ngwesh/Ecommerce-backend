package com.ecommerce.app.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.ecommerce.app.config.RabbitMQConfig;
import com.ecommerce.app.entity.Order;

@Component
public class OrderConsumer {

    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void handleOrder(Order order) {
        System.out.println("Processing order: " + order.getId());
        // send email, call payment service, etc.
    }
}
