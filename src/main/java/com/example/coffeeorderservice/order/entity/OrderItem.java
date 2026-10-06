package com.example.coffeeorderservice.order.entity;

import com.example.coffeeorderservice.menu.entity.Menu;
import com.example.coffeeorderservice.global.exception.BusinessException;
import com.example.coffeeorderservice.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_id", nullable = false, updatable = false)
    private Menu menu;

    @Column(name = "menu_name", nullable = false, updatable = false)
    private String menuName;

    @Column(name = "menu_price", nullable = false, updatable = false)
    private Long menuPrice;

    @Column(nullable = false, updatable = false)
    private Integer quantity;

    OrderItem(Orders order, Menu menu, Integer quantity) {
        this.order = Objects.requireNonNull(order, "주문은 필수입니다.");
        this.menu = Objects.requireNonNull(menu, "메뉴는 필수입니다.");
        if (quantity == null || quantity <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_QUANTITY);
        }
        this.menuName = menu.getName();
        this.menuPrice = menu.getPrice();
        this.quantity = quantity;
    }

    public long getTotalPrice() {
        try {
            return Math.multiplyExact(menuPrice, quantity.longValue());
        } catch (ArithmeticException exception) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_AMOUNT);
        }
    }
}
