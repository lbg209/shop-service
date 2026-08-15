package com.lbg0146.shop_service.member.repository;

import com.lbg0146.shop_service.member.entity.MemberHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberHistoryRepository extends JpaRepository<MemberHistory, Long> {

    List<MemberHistory> findAllByMemberId(Long memberId);

    Optional<MemberHistory> findTopByMemberIdAndValidToIsNullOrderByValidFromDesc(Long memberId);
}
