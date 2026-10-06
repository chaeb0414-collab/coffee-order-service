package com.example.coffeeorderservice.point.entity;

import com.example.coffeeorderservice.member.entity.Member;
import com.example.coffeeorderservice.global.exception.BusinessException;
import com.example.coffeeorderservice.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "point")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, unique = true, updatable = false)
    private Member member;

    @Column(nullable = false)
    private Long balance;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Point(Member member) {
        this.member = Objects.requireNonNull(member, "사용자는 필수입니다.");
        this.balance = 0L;
    }

    public void charge(long amount) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }
        try {
            balance = Math.addExact(balance, amount);
        } catch (ArithmeticException exception) {
            throw new BusinessException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }
    }

    public void deduct(long amount) {
        if (amount < 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_AMOUNT);
        }
        if (balance < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_POINT);
        }
        balance -= amount;
    }

    @PrePersist
    @PreUpdate
    private void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
