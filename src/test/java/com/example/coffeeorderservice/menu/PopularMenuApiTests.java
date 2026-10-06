package com.example.coffeeorderservice.menu;

import com.example.coffeeorderservice.member.entity.Member;
import com.example.coffeeorderservice.member.repository.MemberRepository;
import com.example.coffeeorderservice.menu.entity.Menu;
import com.example.coffeeorderservice.menu.repository.MenuRepository;
import com.example.coffeeorderservice.order.entity.Orders;
import com.example.coffeeorderservice.order.repository.OrdersRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class PopularMenuApiTests {

    @Autowired private WebApplicationContext context;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private OrdersRepository ordersRepository;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private Clock clock;

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 6, 15, 0);
    private final List<Menu> menus = new ArrayList<>();
    private MockMvc mockMvc;
    private Member member;

    @BeforeEach
    void setUp() {
        ZoneId zone = ZoneId.of("Asia/Seoul");
        when(clock.getZone()).thenReturn(zone);
        when(clock.instant()).thenReturn(NOW.atZone(zone).toInstant());
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        member = memberRepository.saveAndFlush(new Member("인기 메뉴 테스트 사용자"));
    }

    @AfterEach
    void tearDown() {
        jdbc.update("delete from order_item where order_id in (select id from orders where member_id = ?)", member.getId());
        jdbc.update("delete from orders where member_id = ?", member.getId());
        memberRepository.deleteById(member.getId());
        menus.forEach(menu -> menuRepository.deleteById(menu.getId()));
    }

    @Test
    void returnsEmptyListWhenThereAreNoOrders() throws Exception {
        menu("주문되지 않은 메뉴");
        mockMvc.perform(get("/api/menus/popular"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(content().json("{\"menus\":[]}"));
    }

    @Test
    void returnsOnlyOrderedMenusWhenFewerThanThreeExist() throws Exception {
        Menu ordered = menu("카페라테");
        menu("주문되지 않은 메뉴");
        order(ordered, 1, NOW.minusHours(1));
        mockMvc.perform(get("/api/menus/popular"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"menus\":[{\"menuId\":" + ordered.getId()
                        + ",\"menuName\":\"카페라테\",\"orderCount\":1}]}"));
    }

    @Test
    void returnsTopThreeByOrderCountDescending() throws Exception {
        Menu first = menu("아메리카노");
        Menu second = menu("카페라테");
        Menu third = menu("바닐라라테");
        Menu fourth = menu("콜드브루");
        repeatOrders(first, 1);
        repeatOrders(second, 4);
        repeatOrders(third, 2);
        repeatOrders(fourth, 3);
        mockMvc.perform(get("/api/menus/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menus.length()").value(3))
                .andExpect(jsonPath("$.menus[0].menuId").value(second.getId()))
                .andExpect(jsonPath("$.menus[0].orderCount").value(4))
                .andExpect(jsonPath("$.menus[1].menuId").value(fourth.getId()))
                .andExpect(jsonPath("$.menus[1].orderCount").value(3))
                .andExpect(jsonPath("$.menus[2].menuId").value(third.getId()))
                .andExpect(jsonPath("$.menus[2].orderCount").value(2));
    }

    @Test
    void breaksTiesByMenuIdAscending() throws Exception {
        Menu first = menu("첫 메뉴");
        Menu second = menu("두 번째 메뉴");
        Menu third = menu("세 번째 메뉴");
        Menu fourth = menu("네 번째 메뉴");
        repeatOrders(fourth, 1);
        repeatOrders(third, 1);
        repeatOrders(second, 1);
        repeatOrders(first, 1);
        mockMvc.perform(get("/api/menus/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menus.length()").value(3))
                .andExpect(jsonPath("$.menus[0].menuId").value(first.getId()))
                .andExpect(jsonPath("$.menus[1].menuId").value(second.getId()))
                .andExpect(jsonPath("$.menus[2].menuId").value(third.getId()));
    }

    @Test
    void includesSevenDayStartButExcludesEndAndFutureOrders() throws Exception {
        Menu menu = menu("경계 검증 메뉴");
        order(menu, 1, NOW.minusDays(7).minusNanos(1000));
        order(menu, 1, NOW.minusDays(7));
        order(menu, 1, NOW.minusNanos(1000));
        order(menu, 1, NOW);
        order(menu, 1, NOW.plusSeconds(1));
        mockMvc.perform(get("/api/menus/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menus.length()").value(1))
                .andExpect(jsonPath("$.menus[0].orderCount").value(2));
    }

    @Test
    void countsOrdersRatherThanNumberOfCups() throws Exception {
        Menu manyCups = menu("한 번에 열 잔");
        Menu moreOrders = menu("두 번 주문");
        order(manyCups, 10, NOW.minusHours(1));
        repeatOrders(moreOrders, 2);
        mockMvc.perform(get("/api/menus/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menus[0].menuId").value(moreOrders.getId()))
                .andExpect(jsonPath("$.menus[0].orderCount").value(2))
                .andExpect(jsonPath("$.menus[1].menuId").value(manyCups.getId()))
                .andExpect(jsonPath("$.menus[1].orderCount").value(1));
    }

    @Test
    void countsSameMenuOnlyOncePerOrderEvenWithMultipleItems() throws Exception {
        Menu menu = menu("중복 항목 메뉴");
        Orders order = order(menu, 1, NOW.minusHours(1));
        jdbc.update("insert into order_item (order_id, menu_id, menu_name, menu_price, quantity) values (?, ?, ?, ?, ?)",
                order.getId(), menu.getId(), menu.getName(), menu.getPrice(), 2);
        mockMvc.perform(get("/api/menus/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menus[0].orderCount").value(1));
    }

    @Test
    void groupsByMenuIdAndReturnsCurrentMenuNameAfterRename() throws Exception {
        Menu menu = menu("기존 메뉴 이름");
        order(menu, 1, NOW.minusDays(2));
        jdbc.update("update menu set name = ? where id = ?", "변경된 메뉴 이름", menu.getId());
        order(menuRepository.findById(menu.getId()).orElseThrow(), 1, NOW.minusHours(1));
        mockMvc.perform(get("/api/menus/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.menus.length()").value(1))
                .andExpect(jsonPath("$.menus[0].menuName").value("변경된 메뉴 이름"))
                .andExpect(jsonPath("$.menus[0].orderCount").value(2));
    }

    private Menu menu(String name) {
        Menu menu = menuRepository.saveAndFlush(new Menu(name, 5000L));
        menus.add(menu);
        return menu;
    }

    private Orders order(Menu menu, int quantity, LocalDateTime orderedAt) {
        Orders order = ordersRepository.saveAndFlush(new Orders(member, menu, quantity));
        jdbc.update("update orders set ordered_at = ? where id = ?", orderedAt, order.getId());
        return order;
    }

    private void repeatOrders(Menu menu, int count) {
        for (int i = 0; i < count; i++) {
            order(menu, 1, NOW.minusHours(1));
        }
    }
}
