package com.example.coffeeorderservice.point;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

@Tag("mysql")
@ActiveProfiles(value = "mysql-test", inheritProfiles = false)
class MySqlPointApiTests extends PointApiTests {
}
