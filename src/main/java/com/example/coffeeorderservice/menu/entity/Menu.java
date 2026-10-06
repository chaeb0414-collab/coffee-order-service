package com.example.coffeeorderservice.menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Menu(String name, Long price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("메뉴 이름은 비어 있을 수 없습니다.");
        }
        if (price == null || price < 0) {
            throw new IllegalArgumentException("메뉴 가격은 0원 이상이어야 합니다.");
        }
        this.name = name;
        this.price = price;
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
