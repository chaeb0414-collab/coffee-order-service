package com.example.coffeeorderservice.entity;

import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ActiveProfiles;

@Tag("mysql")
@ActiveProfiles(value = "mysql-test", inheritProfiles = false)
class MySqlEntityPersistenceTests extends EntityPersistenceTests {
}
