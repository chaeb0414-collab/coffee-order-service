package com.example.coffeeorderservice.point;

import com.example.coffeeorderservice.member.entity.Member;
import com.example.coffeeorderservice.member.repository.MemberRepository;
import com.example.coffeeorderservice.point.entity.Point;
import com.example.coffeeorderservice.point.repository.PointRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class PointApiTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PointRepository pointRepository;

    private MockMvc mockMvc;
    private Long memberId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        Member member = memberRepository.saveAndFlush(new Member("충전 테스트 사용자"));
        memberId = member.getId();
        Point point = new Point(member);
        point.charge(5000);
        pointRepository.saveAndFlush(point);
    }

    @AfterEach
    void tearDown() {
        pointRepository.findByMemberId(memberId).ifPresent(point -> pointRepository.deleteById(point.getId()));
        memberRepository.deleteById(memberId);
    }

    @Test
    void chargesAndCommitsBalanceWithUpdatedTimestamp() throws Exception {
        var previousTime = pointRepository.findByMemberId(memberId).orElseThrow().getUpdatedAt();
        mockMvc.perform(post(chargeUrl()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10000}"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("{\"memberId\":" + memberId
                        + ",\"chargedAmount\":10000,\"balance\":15000}"));
        Point saved = pointRepository.findByMemberId(memberId).orElseThrow();
        assertEquals(15000L, saved.getBalance());
        assertFalse(saved.getUpdatedAt().isBefore(previousTime));
    }

    @Test
    void consecutiveChargesAccumulate() throws Exception {
        mockMvc.perform(post(chargeUrl()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10000}"))
                .andExpect(status().isOk());
        mockMvc.perform(post(chargeUrl()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":3000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(18000));
        assertEquals(18000L, pointRepository.findByMemberId(memberId).orElseThrow().getBalance());
    }

    @Test
    void rejectsMissingPointAccountWithoutCreatingOne() throws Exception {
        pointRepository.deleteById(pointRepository.findByMemberId(memberId).orElseThrow().getId());
        mockMvc.perform(post(chargeUrl()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10000}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POINT_NOT_FOUND"));
        assertTrue(pointRepository.findByMemberId(memberId).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"amount\":0}", "{\"amount\":-1}", "{\"amount\":null}", "{}"})
    void rejectsInvalidAmountWithoutChangingBalance(String body) throws Exception {
        mockMvc.perform(post(chargeUrl()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").isString());
        assertEquals(5000L, pointRepository.findByMemberId(memberId).orElseThrow().getBalance());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"amount\":\"invalid\"}", "{\"amount\":1.5}"})
    void rejectsMalformedRequest(String body) throws Exception {
        mockMvc.perform(post(chargeUrl()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        assertEquals(5000L, pointRepository.findByMemberId(memberId).orElseThrow().getBalance());
    }

    @Test
    void rejectsUnknownMember() throws Exception {
        mockMvc.perform(post("/api/members/" + Long.MAX_VALUE + "/points/charge")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":10000}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEMBER_NOT_FOUND"));
        assertTrue(pointRepository.findByMemberId(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void rejectsBalanceOverflowAndPreservesStoredBalance() throws Exception {
        mockMvc.perform(post(chargeUrl()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":" + Long.MAX_VALUE + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CHARGE_AMOUNT"));
        assertEquals(5000L, pointRepository.findByMemberId(memberId).orElseThrow().getBalance());
    }

    @Test
    void rejectsInvalidMemberIdFormat() throws Exception {
        mockMvc.perform(post("/api/members/invalid/points/charge")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":10000}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    private String chargeUrl() {
        return "/api/members/" + memberId + "/points/charge";
    }
}
