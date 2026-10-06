package com.example.coffeeorderservice.order.service;

import com.example.coffeeorderservice.global.exception.BusinessException;
import com.example.coffeeorderservice.global.exception.ErrorCode;
import com.example.coffeeorderservice.member.entity.Member;
import com.example.coffeeorderservice.member.repository.MemberRepository;
import com.example.coffeeorderservice.menu.entity.Menu;
import com.example.coffeeorderservice.menu.repository.MenuRepository;
import com.example.coffeeorderservice.order.data.OrderData;
import com.example.coffeeorderservice.order.dto.OrderResponse;
import com.example.coffeeorderservice.order.entity.Orders;
import com.example.coffeeorderservice.order.repository.OrdersRepository;
import com.example.coffeeorderservice.point.entity.Point;
import com.example.coffeeorderservice.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final MemberRepository memberRepository;
    private final MenuRepository menuRepository;
    private final PointRepository pointRepository;
    private final OrdersRepository ordersRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResponse create(Long memberId, Long menuId, Integer quantity) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        Point point = pointRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));
        Orders order = new Orders(member, menu, quantity);
        point.deduct(order.getTotalPrice());
        ordersRepository.save(order);
        eventPublisher.publishEvent(new OrderData(order.getId(), memberId, menuId, order.getTotalPrice()));
        return OrderResponse.from(order, point.getBalance());
    }
}
