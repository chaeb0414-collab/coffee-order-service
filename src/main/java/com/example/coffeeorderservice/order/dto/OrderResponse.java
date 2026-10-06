package com.example.coffeeorderservice.order.dto;

import com.example.coffeeorderservice.order.entity.OrderItem;
import com.example.coffeeorderservice.order.entity.OrderStatus;
import com.example.coffeeorderservice.order.entity.Orders;

import java.time.LocalDateTime;

public record OrderResponse(Long orderId, Long memberId, Long menuId, String menuName,
                            Integer quantity, Long totalPrice, Long remainingPoint,
                            OrderStatus status, LocalDateTime orderedAt) {

    public static OrderResponse from(Orders order, Long remainingPoint) {
        OrderItem item = order.getOrderItems().get(0);
        return new OrderResponse(order.getId(), order.getMember().getId(), item.getMenu().getId(),
                item.getMenuName(), item.getQuantity(), order.getTotalPrice(), remainingPoint,
                order.getStatus(), order.getOrderedAt());
    }
}
