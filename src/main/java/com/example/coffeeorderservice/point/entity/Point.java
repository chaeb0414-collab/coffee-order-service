package com.example.coffeeorderservice.point.entity;

import com.example.coffeeorderservice.member.entity.Member;
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
            throw new IllegalArgumentException("충전 금액은 0원보다 커야 합니다.");
        }
        balance = Math.addExact(balance, amount);
    }

    public void deduct(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("차감 금액은 0원 이상이어야 합니다.");
        }
        if (balance < amount) {
            throw new IllegalArgumentException("보유 포인트가 부족합니다.");
        }
        balance -= amount;
    }

    @PrePersist
    @PreUpdate
    private void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
