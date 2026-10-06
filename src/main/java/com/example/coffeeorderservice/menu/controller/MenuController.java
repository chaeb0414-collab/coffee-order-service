package com.example.coffeeorderservice.menu.controller;

import com.example.coffeeorderservice.menu.dto.MenuListResponse;
import com.example.coffeeorderservice.menu.dto.PopularMenuListResponse;
import com.example.coffeeorderservice.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public MenuListResponse getMenus() {
        return menuService.getMenus();
    }

    @GetMapping("/popular")
    public PopularMenuListResponse getPopularMenus() {
        return menuService.getPopularMenus();
    }
}
