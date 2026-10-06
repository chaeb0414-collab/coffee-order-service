package com.example.coffeeorderservice.point.entity;

import com.example.coffeeorderservice.member.entity.Member;
import org.junit.jupiter.api.Test;
import com.example.coffeeorderservice.global.exception.BusinessException;

import static org.junit.jupiter.api.Assertions.*;

class PointTests {

    @Test
    void invalidPointChangesPreserveBalance() {
        Point point = new Point(new Member("사용자"));
        point.charge(10000);
        assertThrows(BusinessException.class, () -> point.charge(0));
        assertThrows(BusinessException.class, () -> point.charge(-1));
        assertThrows(BusinessException.class, () -> point.deduct(-1));
        assertThrows(BusinessException.class, () -> point.deduct(10001));
        assertEquals(10000L, point.getBalance());
        point.deduct(10000);
        assertEquals(0L, point.getBalance());
        point.charge(Long.MAX_VALUE);
        assertThrows(BusinessException.class, () -> point.charge(1));
        assertEquals(Long.MAX_VALUE, point.getBalance());
    }
}
