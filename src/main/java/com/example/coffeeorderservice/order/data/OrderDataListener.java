package com.example.coffeeorderservice.order.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDataListener {

    private final OrderDataSender orderDataSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCompleted(OrderData orderData) {
        try {
            orderDataSender.send(orderData);
        } catch (RuntimeException exception) {
            log.error("주문 데이터 전송 실패: orderId={}", orderData.orderId(), exception);
        }
    }
}
