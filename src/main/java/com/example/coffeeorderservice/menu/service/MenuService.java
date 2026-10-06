package com.example.coffeeorderservice.menu.service;

import com.example.coffeeorderservice.menu.dto.MenuListResponse;
import com.example.coffeeorderservice.menu.dto.MenuResponse;
import com.example.coffeeorderservice.menu.dto.PopularMenuListResponse;
import com.example.coffeeorderservice.menu.repository.MenuRepository;
import com.example.coffeeorderservice.order.entity.OrderStatus;
import org.springframework.data.domain.PageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public MenuListResponse getMenus() {
        return new MenuListResponse(menuRepository.findAllByOrderByIdAsc().stream()
                .map(MenuResponse::from)
                .toList());
    }

    @Transactional(readOnly = true)
    public PopularMenuListResponse getPopularMenus() {
        LocalDateTime end = LocalDateTime.now(clock);
        return new PopularMenuListResponse(menuRepository.findPopularMenus(
                OrderStatus.COMPLETED, end.minusDays(7), end, PageRequest.of(0, 3)));
    }
}
