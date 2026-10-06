package com.example.coffeeorderservice.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotNull @Positive Long memberId,
        @NotNull @Positive Long menuId,
        @NotNull @Positive Integer quantity
) {
}
