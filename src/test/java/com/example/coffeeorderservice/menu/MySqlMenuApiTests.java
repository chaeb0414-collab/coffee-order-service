package com.example.coffeeorderservice.menu;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

@Tag("mysql")
@ActiveProfiles(value = "mysql-test", inheritProfiles = false)
class MySqlMenuApiTests extends MenuApiTests {
}
