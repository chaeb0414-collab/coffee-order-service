package com.example.coffeeorderservice.order.entity;

import com.example.coffeeorderservice.member.entity.Member;
import com.example.coffeeorderservice.menu.entity.Menu;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdersTests {

    @Test
    void invalidOrdersAndPriceOverflowAreRejected() {
        Member member = new Member("사용자");
        Menu menu = new Menu("커피", 4500L);
        assertThrows(IllegalArgumentException.class, () -> new Orders(member, menu, 0));
        assertThrows(IllegalArgumentException.class, () -> new Orders(member, menu, -1));
        assertThrows(ArithmeticException.class, () -> new Orders(member, new Menu("커피", Long.MAX_VALUE), 2));
    }
}
