package com.example.coffeeorderservice.order.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockOrderDataSender implements OrderDataSender {

    @Override
    public void send(OrderData orderData) {
        log.info("주문 데이터 Mock 전송: {}", orderData);
    }
}
