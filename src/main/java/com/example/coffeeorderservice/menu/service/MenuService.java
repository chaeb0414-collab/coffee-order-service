package com.example.coffeeorderservice.menu.service;

import com.example.coffeeorderservice.menu.dto.MenuListResponse;
import com.example.coffeeorderservice.menu.dto.MenuResponse;
import com.example.coffeeorderservice.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    @Transactional(readOnly = true)
    public MenuListResponse getMenus() {
        return new MenuListResponse(menuRepository.findAllByOrderByIdAsc().stream()
                .map(MenuResponse::from)
                .toList());
    }
}
