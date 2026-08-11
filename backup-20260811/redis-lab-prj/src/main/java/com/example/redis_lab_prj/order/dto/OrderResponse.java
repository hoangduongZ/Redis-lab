package com.example.redis_lab_prj.order.dto;

import com.example.redis_lab_prj.order.Order;
import com.example.redis_lab_prj.order.OrderStatus;

import java.time.Instant;

public class OrderResponse {

    private final Long id;
    private final Long productId;
    private final Integer quantity;
    private final OrderStatus status;
    private final Instant createdAt;

    public OrderResponse(Long id, Long productId, Integer quantity, OrderStatus status, Instant createdAt) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.createdAt = createdAt;
    }

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getProduct().getId(),
                order.getQuantity(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
