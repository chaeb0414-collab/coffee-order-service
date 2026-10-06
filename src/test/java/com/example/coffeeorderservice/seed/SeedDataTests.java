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
        jdbcTemplate.update("delete from point where member_id = 1");
        jdbcTemplate.update("delete from member where id = 1");
        jdbcTemplate.update("delete from menu where id in (1, 2, 3)");
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
        assertEquals(3L, jdbcTemplate.queryForObject(
                "select count(*) from menu where id in (1, 2, 3)", Long.class));
    }

    private void seed() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding("UTF-8");
        populator.addScript(new FileSystemResource("docs/sql/seed.sql"));
        populator.execute(dataSource);
    }
}
