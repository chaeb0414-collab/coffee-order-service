package com.example.coffeeorderservice.order.entity;

import com.example.coffeeorderservice.member.entity.Member;
import com.example.coffeeorderservice.menu.entity.Menu;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, updatable = false)
    private Member member;

    @Column(name = "total_price", nullable = false, updatable = false)
    private Long totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "ordered_at", nullable = false, updatable = false)
    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.PERSIST)
    @Getter(AccessLevel.NONE)
    private List<OrderItem> orderItems = new ArrayList<>();

    public Orders(Member member, Menu menu, Integer quantity) {
        this.member = Objects.requireNonNull(member, "사용자는 필수입니다.");
        OrderItem item = new OrderItem(this, menu, quantity);
        this.orderItems.add(item);
        this.totalPrice = item.getTotalPrice();
        this.status = OrderStatus.COMPLETED;
        this.orderedAt = LocalDateTime.now();
    }

    public List<OrderItem> getOrderItems() {
        return Collections.unmodifiableList(orderItems);
    }
}
