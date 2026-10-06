package com.example.coffeeorderservice.menu.repository;

import com.example.coffeeorderservice.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByOrderByIdAsc();
}
