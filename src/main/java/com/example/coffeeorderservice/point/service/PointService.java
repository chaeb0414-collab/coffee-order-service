package com.example.coffeeorderservice.point.service;

import com.example.coffeeorderservice.global.exception.BusinessException;
import com.example.coffeeorderservice.global.exception.ErrorCode;
import com.example.coffeeorderservice.member.repository.MemberRepository;
import com.example.coffeeorderservice.point.dto.ChargePointResponse;
import com.example.coffeeorderservice.point.entity.Point;
import com.example.coffeeorderservice.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final MemberRepository memberRepository;
    private final PointRepository pointRepository;

    @Transactional
    public ChargePointResponse charge(Long memberId, Long amount) {
        if (amount == null) {
            throw new BusinessException(ErrorCode.INVALID_CHARGE_AMOUNT);
        }
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }
        Point point = pointRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POINT_NOT_FOUND));
        point.charge(amount);
        return new ChargePointResponse(memberId, amount, point.getBalance());
    }
}
