package com.example.coffeeorderservice.menu;

import com.example.coffeeorderservice.menu.entity.Menu;
import com.example.coffeeorderservice.menu.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MenuApiTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MenuRepository menuRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void returnsMenusInIdOrderWithOnlyResponseFields() throws Exception {
        Menu first = menuRepository.saveAndFlush(new Menu("카페라테", 5000L));
        Menu second = menuRepository.saveAndFlush(new Menu("아메리카노", 4500L));

        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.menus.length()").value(2))
                .andExpect(jsonPath("$.menus[0].menuId").value(first.getId()))
                .andExpect(jsonPath("$.menus[0].name").value("카페라테"))
                .andExpect(jsonPath("$.menus[0].price").value(5000))
                .andExpect(jsonPath("$.menus[1].menuId").value(second.getId()))
                .andExpect(jsonPath("$.menus[1].name").value("아메리카노"))
                .andExpect(jsonPath("$.menus[1].price").value(4500))
                .andExpect(jsonPath("$.menus[0].length()").value(3))
                .andExpect(jsonPath("$.menus[0].createdAt").doesNotExist());
    }

    @Test
    void returnsEmptyArrayWhenNoMenusExist() throws Exception {
        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"menus\":[]}"));
    }
}
