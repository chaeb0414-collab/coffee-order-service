package com.example.coffeeorderservice.order;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

@Tag("mysql")
@ActiveProfiles(value = "mysql-test", inheritProfiles = false)
class MySqlOrderApiTests extends OrderApiTests {
}
