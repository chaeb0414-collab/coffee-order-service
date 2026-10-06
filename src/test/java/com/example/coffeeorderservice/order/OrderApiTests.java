package com.example.coffeeorderservice.order;

import com.example.coffeeorderservice.member.entity.Member;
import com.example.coffeeorderservice.member.repository.MemberRepository;
import com.example.coffeeorderservice.menu.entity.Menu;
import com.example.coffeeorderservice.menu.repository.MenuRepository;
import com.example.coffeeorderservice.order.data.OrderData;
import com.example.coffeeorderservice.order.data.OrderDataSender;
import com.example.coffeeorderservice.order.entity.Orders;
import com.example.coffeeorderservice.order.repository.OrdersRepository;
import com.example.coffeeorderservice.order.service.OrderService;
import com.example.coffeeorderservice.point.entity.Point;
import com.example.coffeeorderservice.point.repository.PointRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderApiTests {

    @Autowired private WebApplicationContext context;
    @Autowired private MemberRepository memberRepository;
    @Autowired private MenuRepository menuRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private OrderService orderService;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;
    @MockitoBean private OrderDataSender sender;
    @MockitoSpyBean private OrdersRepository ordersRepository;

    private MockMvc mockMvc;
    private Member member;
    private Menu menu;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        member = memberRepository.saveAndFlush(new Member("주문 테스트 사용자"));
        menu = menuRepository.saveAndFlush(new Menu("카페라테", 5000L));
        Point point = new Point(member);
        point.charge(15000);
        pointRepository.saveAndFlush(point);
    }

    @AfterEach
    void tearDown() {
        jdbc.update("delete from order_item where order_id in (select id from orders where member_id = ?)", member.getId());
        jdbc.update("delete from orders where member_id = ?", member.getId());
        pointRepository.findByMemberId(member.getId()).ifPresent(p -> pointRepository.deleteById(p.getId()));
        memberRepository.deleteById(member.getId());
        menuRepository.deleteById(menu.getId());
    }

    @Test
    void commitsOrderItemsAndPointsThenSendsCorrectData() throws Exception {
        doAnswer(invocation -> {
            OrderData data = invocation.getArgument(0);
            assertTrue(ordersRepository.existsById(data.orderId()));
            assertEquals(5000L, balance());
            return null;
        }).when(sender).send(any());

        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").isNumber())
                .andExpect(jsonPath("$.memberId").value(member.getId()))
                .andExpect(jsonPath("$.menuId").value(menu.getId()))
                .andExpect(jsonPath("$.menuName").value("카페라테"))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(10000))
                .andExpect(jsonPath("$.remainingPoint").value(5000))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.orderedAt").isString());

        ArgumentCaptor<OrderData> captor = ArgumentCaptor.forClass(OrderData.class);
        verify(sender).send(captor.capture());
        OrderData data = captor.getValue();
        assertEquals(member.getId(), data.memberId());
        assertEquals(menu.getId(), data.menuId());
        assertEquals(10000L, data.totalPrice());
        assertEquals(1L, orderCount());
        assertEquals("카페라테", jdbc.queryForObject("select menu_name from order_item where order_id = ?", String.class, data.orderId()));
        assertEquals(5000L, jdbc.queryForObject("select menu_price from order_item where order_id = ?", Long.class, data.orderId()));
        assertEquals(2, jdbc.queryForObject("select quantity from order_item where order_id = ?", Integer.class, data.orderId()));
    }

    @Test
    void allowsPaymentUsingEntireBalance() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(3)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.remainingPoint").value(0));
        assertEquals(0L, balance());
        verify(sender).send(any());
    }

    @Test
    void rejectsInsufficientPointsWithoutSavingOrSending() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(4)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INSUFFICIENT_POINT"));
        assertUnchanged();
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "null", "1.5"})
    void rejectsInvalidQuantity(String quantity) throws Exception {
        String request = "{\"memberId\":" + member.getId() + ",\"menuId\":" + menu.getId() + ",\"quantity\":" + quantity + "}";
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        assertUnchanged();
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{", "{\"memberId\":1,\"menuId\":1}"})
    void rejectsIncompleteOrMalformedRequest(String request) throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        assertUnchanged();
    }

    @Test
    void rejectsUnknownMember() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":" + Long.MAX_VALUE + ",\"menuId\":" + menu.getId() + ",\"quantity\":1}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("MEMBER_NOT_FOUND"));
        assertUnchanged();
    }

    @Test
    void rejectsUnknownMenu() throws Exception {
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"memberId\":" + member.getId() + ",\"menuId\":" + Long.MAX_VALUE + ",\"quantity\":1}"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("MENU_NOT_FOUND"));
        assertUnchanged();
    }

    @Test
    void rejectsMissingPointAccount() throws Exception {
        pointRepository.deleteById(pointRepository.findByMemberId(member.getId()).orElseThrow().getId());
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(1)))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.code").value("POINT_NOT_FOUND"));
        assertEquals(0L, orderCount());
        verifyNoInteractions(sender);
    }

    @Test
    void rejectsPriceOverflow() throws Exception {
        jdbc.update("update menu set price = ? where id = ?", Long.MAX_VALUE, menu.getId());
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(2)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_ORDER_AMOUNT"));
        assertUnchanged();
    }

    @Test
    void saveFailureRollsBackPointsAndDoesNotSend() {
        doThrow(new IllegalStateException("주문 저장 실패")).when(ordersRepository).save(any(Orders.class));
        assertThrows(IllegalStateException.class, () -> orderService.create(member.getId(), menu.getId(), 1));
        assertUnchanged();
    }

    @Test
    void saveFailureReturnsCommonServerErrorWithoutExposingDetails() throws Exception {
        doThrow(new IllegalStateException("주문 저장 내부 오류")).when(ordersRepository).save(any(Orders.class));
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(1)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json("{\"code\":\"INTERNAL_SERVER_ERROR\",\"message\":\"요청 처리 중 오류가 발생했습니다.\"}"));
        assertUnchanged();
    }

    @Test
    void unsupportedHttpMethodRemainsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void enclosingTransactionRollbackRemovesOrderAndItemsAndDoesNotSend() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            orderService.create(member.getId(), menu.getId(), 1);
            ordersRepository.flush();
            verifyNoInteractions(sender);
            status.setRollbackOnly();
        });
        assertUnchanged();
        assertEquals(0L, jdbc.queryForObject("select count(*) from order_item where menu_id = ?", Long.class, menu.getId()));
    }

    @Test
    void senderFailureKeepsCommittedPaymentAndSuccessfulResponse() throws Exception {
        doThrow(new IllegalStateException("Mock 전송 실패")).when(sender).send(any());
        mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(body(1)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.remainingPoint").value(10000));
        assertEquals(10000L, balance());
        assertEquals(1L, orderCount());
        verify(sender).send(any());
    }

    private String body(int quantity) {
        return "{\"memberId\":" + member.getId() + ",\"menuId\":" + menu.getId() + ",\"quantity\":" + quantity + "}";
    }

    private long balance() {
        return pointRepository.findByMemberId(member.getId()).orElseThrow().getBalance();
    }

    private long orderCount() {
        return jdbc.queryForObject("select count(*) from orders where member_id = ?", Long.class, member.getId());
    }

    private void assertUnchanged() {
        assertEquals(15000L, balance());
        assertEquals(0L, orderCount());
        verifyNoInteractions(sender);
    }
}
