package com.example.coffeeorderservice.menu.repository;

import com.example.coffeeorderservice.menu.entity.Menu;
import com.example.coffeeorderservice.menu.dto.PopularMenuResponse;
import com.example.coffeeorderservice.order.entity.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findAllByOrderByIdAsc();

    @Query("""
            select new com.example.coffeeorderservice.menu.dto.PopularMenuResponse(
                item.menu.id, item.menu.name, count(distinct item.order.id))
            from OrderItem item
            where item.order.status = :status
              and item.order.orderedAt >= :start
              and item.order.orderedAt < :end
            group by item.menu.id, item.menu.name
            order by count(distinct item.order.id) desc, item.menu.id asc
            """)
    List<PopularMenuResponse> findPopularMenus(@Param("status") OrderStatus status,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               Pageable pageable);
}
