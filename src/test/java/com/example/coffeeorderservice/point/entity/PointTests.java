package com.example.coffeeorderservice.point.entity;

import com.example.coffeeorderservice.member.entity.Member;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PointTests {

    @Test
    void invalidPointChangesPreserveBalance() {
        Point point = new Point(new Member("사용자"));
        point.charge(10000);
        assertThrows(IllegalArgumentException.class, () -> point.charge(0));
        assertThrows(IllegalArgumentException.class, () -> point.charge(-1));
        assertThrows(IllegalArgumentException.class, () -> point.deduct(-1));
        assertThrows(IllegalArgumentException.class, () -> point.deduct(10001));
        assertEquals(10000L, point.getBalance());
        point.deduct(10000);
        assertEquals(0L, point.getBalance());
        point.charge(Long.MAX_VALUE);
        assertThrows(ArithmeticException.class, () -> point.charge(1));
        assertEquals(Long.MAX_VALUE, point.getBalance());
    }
}
