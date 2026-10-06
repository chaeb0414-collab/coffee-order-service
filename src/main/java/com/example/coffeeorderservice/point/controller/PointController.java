package com.example.coffeeorderservice.point.controller;

import com.example.coffeeorderservice.point.dto.ChargePointRequest;
import com.example.coffeeorderservice.point.dto.ChargePointResponse;
import com.example.coffeeorderservice.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/{memberId}/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/charge")
    public ChargePointResponse charge(@PathVariable("memberId") Long memberId,
                                      @Valid @RequestBody ChargePointRequest request) {
        return pointService.charge(memberId, request.amount());
    }
}
