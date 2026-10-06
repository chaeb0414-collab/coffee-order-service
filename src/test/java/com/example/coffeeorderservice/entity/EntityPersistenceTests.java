package com.example.coffeeorderservice.entity;

import com.example.coffeeorderservice.member.entity.Member;
import com.example.coffeeorderservice.menu.entity.Menu;
import com.example.coffeeorderservice.order.entity.OrderItem;
import com.example.coffeeorderservice.order.entity.OrderStatus;
import com.example.coffeeorderservice.order.entity.Orders;
import com.example.coffeeorderservice.point.entity.Point;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class EntityPersistenceTests {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private SessionFactory sessionFactory;

    @BeforeEach
    void setUp() {
        sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from order_item");
        jdbcTemplate.update("delete from orders");
        jdbcTemplate.update("delete from point");
        jdbcTemplate.update("delete from menu");
        jdbcTemplate.update("delete from member");
    }

    @Test
    void savesOrderWithItemsAndTimestamps() {
        Member member = new Member("사용자");
        Menu menu = new Menu("카페라테", 5000L);
        Point point = new Point(member);
        point.charge(15000);
        Orders order = new Orders(member, menu, 2);
        point.deduct(order.getTotalPrice());

        sessionFactory.inTransaction(session -> {
            session.persist(member);
            session.persist(menu);
            session.persist(point);
            session.persist(order);
        });

        sessionFactory.inTransaction(session -> {
            Orders saved = session.find(Orders.class, order.getId());
            assertEquals(10000L, saved.getTotalPrice());
            assertEquals(OrderStatus.COMPLETED, saved.getStatus());
            assertNotNull(saved.getOrderedAt());
            assertEquals(member.getId(), saved.getMember().getId());
            assertEquals(1, saved.getOrderItems().size());
            OrderItem item = saved.getOrderItems().get(0);
            assertNotNull(item.getId());
            assertSame(saved, item.getOrder());
            assertEquals(menu.getId(), item.getMenu().getId());
            assertEquals("카페라테", item.getMenuName());
            assertEquals(5000L, item.getMenuPrice());
            assertEquals(2, item.getQuantity());
            Point savedPoint = session.find(Point.class, point.getId());
            assertEquals(5000L, savedPoint.getBalance());
            assertNotNull(savedPoint.getUpdatedAt());
            assertNotNull(session.find(Member.class, member.getId()).getCreatedAt());
            assertNotNull(session.find(Menu.class, menu.getId()).getCreatedAt());
            assertThrows(UnsupportedOperationException.class, () -> saved.getOrderItems().clear());
        });
    }

    @Test
    void menuChangesDoNotChangeOrderSnapshot() {
        Member member = new Member("사용자");
        Menu menu = new Menu("카페라테", 5000L);
        Orders order = new Orders(member, menu, 2);
        sessionFactory.inTransaction(session -> {
            session.persist(member);
            session.persist(menu);
            session.persist(order);
        });
        sessionFactory.inTransaction(session -> session.createMutationQuery(
                "update Menu set name = :name, price = :price where id = :id")
                .setParameter("name", "변경된 메뉴")
                .setParameter("price", 6000L)
                .setParameter("id", menu.getId())
                .executeUpdate());
        sessionFactory.inTransaction(session -> {
            Orders saved = session.find(Orders.class, order.getId());
            OrderItem item = saved.getOrderItems().get(0);
            assertEquals(6000L, item.getMenu().getPrice());
            assertEquals("카페라테", item.getMenuName());
            assertEquals(5000L, item.getMenuPrice());
            assertEquals(10000L, saved.getTotalPrice());
        });
    }

    @Test
    void memberCannotHaveTwoPointAccounts() {
        Member member = new Member("사용자");
        sessionFactory.inTransaction(session -> {
            session.persist(member);
            session.persist(new Point(member));
        });
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            assertThrows(jakarta.persistence.PersistenceException.class, () -> {
                session.persist(new Point(session.find(Member.class, member.getId())));
                session.flush();
            });
            session.getTransaction().rollback();
        }
    }

    @Test
    void rollbackRestoresPointsAndRemovesOrderAndItems() {
        Member member = new Member("사용자");
        Menu menu = new Menu("커피", 4500L);
        Point point = new Point(member);
        point.charge(10000);
        sessionFactory.inTransaction(session -> {
            session.persist(member);
            session.persist(menu);
            session.persist(point);
        });
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Point savedPoint = session.find(Point.class, point.getId());
            Orders order = new Orders(session.find(Member.class, member.getId()),
                    session.find(Menu.class, menu.getId()), 1);
            savedPoint.deduct(order.getTotalPrice());
            session.persist(order);
            session.flush();
            session.getTransaction().rollback();
        }
        sessionFactory.inTransaction(session -> {
            assertEquals(10000L, session.find(Point.class, point.getId()).getBalance());
            assertEquals(0L, session.createSelectionQuery("select count(o) from Orders o", Long.class).getSingleResult());
            assertEquals(0L, session.createSelectionQuery("select count(i) from OrderItem i", Long.class).getSingleResult());
        });
    }

}
