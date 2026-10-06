package com.example.coffeeorderservice.seed;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class SeedDataTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from order_item where order_id in (select id from orders where member_id = 100001)");
        jdbcTemplate.update("delete from orders where member_id = 100001");
        jdbcTemplate.update("delete from point where member_id = 100001");
        jdbcTemplate.update("delete from member where id = 100001");
        jdbcTemplate.update("delete from point where member_id = 1");
        jdbcTemplate.update("delete from member where id = 1");
        jdbcTemplate.update("delete from menu where id in (1, 2, 3, 4, 5)");
    }

    @Test
    void seedsInitialDataAndPreservesExistingValuesOnRerun() {
        seed();
        assertEquals("테스트 사용자", jdbcTemplate.queryForObject(
                "select name from member where id = 1", String.class));
        assertEquals(0L, jdbcTemplate.queryForObject(
                "select balance from point where member_id = 1", Long.class));
        assertEquals(4500L, jdbcTemplate.queryForObject("select price from menu where id = 1", Long.class));
        assertEquals(5000L, jdbcTemplate.queryForObject("select price from menu where id = 2", Long.class));
        assertEquals(5500L, jdbcTemplate.queryForObject("select price from menu where id = 3", Long.class));
        assertEquals("카푸치노", jdbcTemplate.queryForObject("select name from menu where id = 4", String.class));
        assertEquals(5000L, jdbcTemplate.queryForObject("select price from menu where id = 4", Long.class));
        assertEquals("카페모카", jdbcTemplate.queryForObject("select name from menu where id = 5", String.class));
        assertEquals(5500L, jdbcTemplate.queryForObject("select price from menu where id = 5", Long.class));

        jdbcTemplate.update("update point set balance = 10000 where member_id = 1");
        jdbcTemplate.update("update menu set price = 4700 where id = 1");
        jdbcTemplate.update("update member set name = '변경된 사용자' where id = 1");
        seed();

        assertEquals(10000L, jdbcTemplate.queryForObject(
                "select balance from point where member_id = 1", Long.class));
        assertEquals(4700L, jdbcTemplate.queryForObject("select price from menu where id = 1", Long.class));
        assertEquals("변경된 사용자", jdbcTemplate.queryForObject(
                "select name from member where id = 1", String.class));
        assertEquals(1L, jdbcTemplate.queryForObject(
                "select count(*) from point where member_id = 1", Long.class));
        assertEquals(5L, jdbcTemplate.queryForObject(
                "select count(*) from menu where id in (1, 2, 3, 4, 5)", Long.class));
    }

    @Test
    void sampleOrdersAreConsistentAndNotDuplicatedOnRerun() {
        seed();
        executeSql("sql/sample-orders.sql");
        executeSql("sql/sample-orders.sql");
        assertEquals(12L, jdbcTemplate.queryForObject(
                "select count(*) from orders where member_id = 100001", Long.class));
        assertEquals(12L, jdbcTemplate.queryForObject(
                "select count(*) from order_item i join orders o on o.id = i.order_id where o.member_id = 100001", Long.class));
        assertEquals(0L, jdbcTemplate.queryForObject(
                "select count(*) from orders o join order_item i on i.order_id = o.id where o.member_id = 100001 and o.total_price <> i.menu_price * i.quantity", Long.class));
        assertEquals(5L, jdbcTemplate.queryForObject(
                "select count(distinct order_id) from order_item where menu_id = 1", Long.class));
        assertEquals(3L, jdbcTemplate.queryForObject(
                "select count(distinct order_id) from order_item where menu_id = 2", Long.class));
        assertEquals(2L, jdbcTemplate.queryForObject(
                "select count(distinct order_id) from order_item where menu_id = 3", Long.class));
        assertEquals(0L, jdbcTemplate.queryForObject(
                "select balance from point where member_id = 1", Long.class));
    }

    private void seed() {
        executeSql("sql/initial-data.sql");
    }

    private void executeSql(String path) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding("UTF-8");
        populator.addScript(new FileSystemResource(path));
        populator.execute(dataSource);
    }
}
