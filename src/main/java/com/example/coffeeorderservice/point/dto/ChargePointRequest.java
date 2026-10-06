package com.example.coffeeorderservice.point.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChargePointRequest(
        @NotNull(message = "충전 금액은 필수입니다.")
        @Positive(message = "충전 금액은 0원보다 커야 합니다.")
        Long amount
) {
}
