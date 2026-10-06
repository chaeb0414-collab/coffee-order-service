package com.example.coffeeorderservice.member.repository;

import com.example.coffeeorderservice.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
