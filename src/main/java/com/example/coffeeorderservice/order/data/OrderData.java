package com.example.coffeeorderservice.order.data;

public record OrderData(Long orderId, Long memberId, Long menuId, Long totalPrice) {
}
